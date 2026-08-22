package com.adferdv.ktile

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import com.adferdv.ktile.core.comms.AppSocketComms
import com.adferdv.ktile.core.persistence.repo.SettingsRepository
import com.adferdv.ktile.core.screen.isLinux
import com.adferdv.ktile.ui.InputPermissionWarningDialog
import com.adferdv.ktile.ui.KTileTray
import com.adferdv.ktile.ui.KTileWindow
import com.adferdv.ktile.ui.SettingsWindow
import com.adferdv.ktile.ui.createTrayIcon
import com.adferdv.ktile.ui.globalHotkeyRegistration
import com.adferdv.ktile.viewmodel.SettingsViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.logging.Logger

private val logger = Logger.getLogger("com.adferdv.ktile.Main")

fun main() {
    val settingsRequestChannel = Channel<Unit>(Channel.CONFLATED)
    val acquired =
        AppSocketComms.tryAcquireServer { command ->
            if (command == AppSocketComms.COMMAND_SHOW_SETTINGS) {
                settingsRequestChannel.trySend(Unit)
            }
        }

    if (!acquired) {
        return
    }

    runApplication(settingsRequestChannel)
}

private fun runApplication(settingsRequestChannel: Channel<Unit>) {
    val settingsRepository =
        runCatching { SettingsRepository() }
            .onFailure { logger.warning("Failed to initialize settings repository: ${it.message}") }
            .getOrNull()

    application(exitProcessOnExit = false) {
        val settingsCoroutineScope = rememberCoroutineScope()
        val settingsViewModel = remember { SettingsViewModel(settingsCoroutineScope, settingsRepository) }
        var isWindowVisible by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showPermissionWarning by remember { mutableStateOf(false) }
        val trayIcon = remember { createTrayIcon() }

        LaunchedEffect(Unit) {
            settingsRequestChannel.consumeEach { showSettings = true }
        }

        globalHotkeyRegistration(
            settingsViewModel = settingsViewModel,
            onPermissionMissing = { showPermissionWarning = true },
            onToggle = { isWindowVisible = !isWindowVisible },
        )

        KTileWindow(
            visible = isWindowVisible,
            onClose = { isWindowVisible = false },
            viewModel = settingsViewModel,
        )

        if (!isLinux() && isTraySupported) {
            KTileTray(
                icon = trayIcon,
                onToggle = { isWindowVisible = !isWindowVisible },
                onSettings = { showSettings = true },
            )
        }

        if (showSettings) {
            SettingsWindow(
                isVisible = showSettings,
                onClose = { showSettings = false },
                viewModel = settingsViewModel,
            )
        }

        if (showPermissionWarning) {
            InputPermissionWarningDialog(onDismiss = { showPermissionWarning = false })
        }
    }
}
