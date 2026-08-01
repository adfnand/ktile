package com.adferdv.ktile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import com.adferdv.ktile.core.hotkey.GlobalHotkeyProvider
import com.adferdv.ktile.core.hotkey.Hotkey
import com.adferdv.ktile.core.hotkey.JNativeHookProvider
import com.adferdv.ktile.core.instance.SingleInstanceToggle
import com.adferdv.ktile.core.screen.FullscreenHelper
import com.adferdv.ktile.ui.LayoutPreviewScreen
import com.adferdv.ktile.ui.createTrayIcon
import com.adferdv.ktile.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import java.awt.GraphicsEnvironment
import javax.swing.SwingUtilities
import kotlin.time.Duration.Companion.milliseconds

private const val SHOW_POLL_INTERVAL_MS = 100L
private const val FULLSCREEN_WAIT_TIMEOUT_MS = 2_000L

fun main() {
    val singleInstance = SingleInstanceToggle {}
    if (singleInstance.trySendToggleAndExit()) {
        return
    }

    if (isWayland()) {
        println(
            "KTile: running on Wayland. Global hotkeys are not supported by the app. " +
                "Set a desktop shortcut (Super+K) to launch KTile to toggle the preview.",
        )
    }

    application(exitProcessOnExit = false) {
        val settingsCoroutineScope = rememberCoroutineScope()
        val settingsViewModel = remember { SettingsViewModel(settingsCoroutineScope) }
        var isWindowVisible by remember { mutableStateOf(false) }

        val hotkeyProvider = rememberGlobalHotkeyProvider()
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
                    isWindowVisible = !isWindowVisible
                }
            }
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
    }
}

@Composable
private fun rememberGlobalHotkeyProvider(): GlobalHotkeyProvider? =
    remember {
        if (GraphicsEnvironment.isHeadless() || isWayland()) {
            null
        } else {
            JNativeHookProvider()
        }
    }

private fun isWayland(): Boolean {
    val sessionType = System.getenv("XDG_SESSION_TYPE")
    val waylandDisplay = System.getenv("WAYLAND_DISPLAY")
    return sessionType == "wayland" || waylandDisplay != null
}

@Suppress("FunctionNaming")
@Composable
private fun KTileWindow(
    visible: Boolean,
    onClose: () -> Unit,
    viewModel: SettingsViewModel,
) {
    Window(
        visible = visible,
        onCloseRequest = onClose,
        undecorated = true,
        transparent = true,
        title = FullscreenHelper.WINDOW_TITLE,
    ) {
        var previewReady by remember { mutableStateOf(false) }

        LaunchedEffect(visible) {
            if (!visible) {
                previewReady = false
                return@LaunchedEffect
            }

            while (!window.isShowing) {
                delay(SHOW_POLL_INTERVAL_MS.milliseconds)
            }
            FullscreenHelper.enterFullscreen(window, FULLSCREEN_WAIT_TIMEOUT_MS)
            previewReady = true
        }

        if (previewReady) {
            LayoutPreviewScreen(viewModel)
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
