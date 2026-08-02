package com.adferdv.ktile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import com.adferdv.ktile.core.hotkey.GlobalHotkeyProvider
import com.adferdv.ktile.core.hotkey.Hotkey
import com.adferdv.ktile.core.hotkey.InputDevicePermissionChecker
import com.adferdv.ktile.core.hotkey.JNativeHookProvider
import com.adferdv.ktile.core.hotkey.LinuxEvdevHotkeyProvider
import com.adferdv.ktile.core.instance.SingleInstanceToggle
import com.adferdv.ktile.core.screen.FullscreenHelper
import com.adferdv.ktile.ui.InputPermissionWarningDialog
import com.adferdv.ktile.ui.LayoutPreviewScreen
import com.adferdv.ktile.ui.createTrayIcon
import com.adferdv.ktile.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.GraphicsEnvironment
import java.util.logging.Logger
import javax.swing.SwingUtilities
import kotlin.time.Duration.Companion.milliseconds

private const val SHOW_POLL_INTERVAL_MS = 100L
private const val FULLSCREEN_WAIT_TIMEOUT_MS = 2_000L

private val logger = Logger.getLogger("com.adferdv.ktile.Main")

fun main() {
    val singleInstance = SingleInstanceToggle {}
    if (singleInstance.trySendToggleAndExit()) {
        return
    }

    application(exitProcessOnExit = false) {
        val settingsCoroutineScope = rememberCoroutineScope()
        val settingsViewModel = remember { SettingsViewModel(settingsCoroutineScope) }
        var isWindowVisible by remember { mutableStateOf(false) }
        var showPermissionWarning by remember { mutableStateOf(false) }

        val hotkeyProvider = rememberGlobalHotkeyProvider(onPermissionMissing = { showPermissionWarning = true })
        val trayIcon = remember { createTrayIcon() }

        DisposableEffect(Unit) {
            singleInstance.setCallback {
                SwingUtilities.invokeLater {
                    isWindowVisible = !isWindowVisible
                }
            }
            singleInstance.startServer()
            onDispose { singleInstance.stopServer() }
        }

        LaunchedEffect(hotkeyProvider) {
            hotkeyProvider?.register(Hotkey.DEFAULT_TOGGLE) {
                SwingUtilities.invokeLater {
                    System.err.println("Hotkey callback: toggling window from visible=$isWindowVisible")
                    isWindowVisible = !isWindowVisible
                }
            }
        }

        LaunchedEffect(isWindowVisible) {
            System.err.println("Main: isWindowVisible changed to $isWindowVisible")
        }
        KTileWindow(
            visible = isWindowVisible,
            onClose = { isWindowVisible = false },
            viewModel = settingsViewModel,
        )

        if (isTraySupported) {
            KTileTray(
                icon = trayIcon,
                onToggle = { isWindowVisible = !isWindowVisible },
            )
        }

        if (showPermissionWarning) {
            InputPermissionWarningDialog(onDismiss = { showPermissionWarning = false })
        }
    }
}

@Composable
private fun rememberGlobalHotkeyProvider(onPermissionMissing: () -> Unit): GlobalHotkeyProvider? =
    remember {
        when {
            GraphicsEnvironment.isHeadless() -> null
            isLinux() -> createLinuxProvider(onPermissionMissing)
            else -> JNativeHookProvider()
        }
    }

private fun createLinuxProvider(onPermissionMissing: () -> Unit): GlobalHotkeyProvider? =
    try {
        if (!InputDevicePermissionChecker.hasInputDeviceAccess()) {
            onPermissionMissing()
        }
        LinuxEvdevHotkeyProvider()
    } catch (e: IllegalStateException) {
        logger.warning("Failed to initialize Linux evdev hotkey provider: ${e.message}")
        onPermissionMissing()
        null
    } catch (e: UnsatisfiedLinkError) {
        logger.warning("Failed to load Linux evdev hotkey native library: ${e.message}")
        onPermissionMissing()
        null
    }

private fun isLinux(): Boolean = System.getProperty("os.name").lowercase().contains("linux")

@Suppress("FunctionNaming")
@Composable
private fun KTileWindow(
    visible: Boolean,
    onClose: () -> Unit,
    viewModel: SettingsViewModel,
) {
    // The underlying Window is always composed (visible = true here). Toggling
    // Compose's own `visible` param unmaps the window and pauses its
    // frame/recomposition loop while hidden, which caused LaunchedEffect(visible)
    // to miss transitions on rapid toggles. Instead we keep the window alive at
    // all times and drive the *actual* native show/hide state imperatively via
    // the ComposeWindow reference below, so it's never gated behind a paused
    // recomposition loop.
    var composeWindow by remember { mutableStateOf<ComposeWindow?>(null) }
    var previewReady by remember { mutableStateOf(false) }

    Window(
        visible = true,
        onCloseRequest = onClose,
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        title = FullscreenHelper.WINDOW_TITLE,
    ) {
        LaunchedEffect(Unit) {
            composeWindow = window
            window.isVisible = false
        }

        if (previewReady) {
            LayoutPreviewScreen(viewModel)
        }
    }

    LaunchedEffect(visible, composeWindow) {
        val win = composeWindow ?: return@LaunchedEffect
        println("KTileWindow: visible=$visible")

        if (!visible) {
            previewReady = false
            win.isVisible = false
            return@LaunchedEffect
        }

        System.err.println("KTileWindow: showing window, waiting for window.isShowing")
        win.isVisible = true
        while (!win.isShowing) {
            delay(SHOW_POLL_INTERVAL_MS.milliseconds)
        }
        System.err.println("KTileWindow: window is showing, toFront+requestFocus")
        win.toFront()
        win.requestFocus()
        previewReady = true
        System.err.println("KTileWindow: preview ready")

        launch {
            val fullscreenApplied = FullscreenHelper.enterFullscreen(win, FULLSCREEN_WAIT_TIMEOUT_MS)
            System.err.println("KTileWindow: fullscreen applied=$fullscreenApplied")
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ApplicationScope.KTileTray(
    icon: Painter,
    onToggle: () -> Unit,
) {
    Tray(
        icon = icon,
        tooltip = "KTile",
        onAction = onToggle,
        menu = {
            Item("Toggle KTile", onClick = onToggle)
            Item("Quit", onClick = ::exitApplication)
        },
    )
}
