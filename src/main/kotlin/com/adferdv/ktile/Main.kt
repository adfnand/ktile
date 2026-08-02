package com.adferdv.ktile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import com.adferdv.ktile.core.hotkey.GlobalHotkeyProvider
import com.adferdv.ktile.core.hotkey.Hotkey
import com.adferdv.ktile.core.hotkey.InputDevicePermissionChecker
import com.adferdv.ktile.core.hotkey.JNativeHookProvider
import com.adferdv.ktile.core.hotkey.LinuxEvdevHotkeyProvider
import com.adferdv.ktile.core.instance.SingleInstanceToggle
import com.adferdv.ktile.core.screen.isLinux
import com.adferdv.ktile.ui.InputPermissionWarningDialog
import com.adferdv.ktile.ui.KTileTray
import com.adferdv.ktile.ui.KTileWindow
import com.adferdv.ktile.ui.createTrayIcon
import com.adferdv.ktile.viewmodel.SettingsViewModel
import java.awt.GraphicsEnvironment
import java.util.logging.Logger
import javax.swing.SwingUtilities

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
