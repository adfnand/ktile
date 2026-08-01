package com.adferdv.ktile

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.adferdv.ktile.core.screen.FullscreenHelper
import com.adferdv.ktile.ui.LayoutPreviewScreen
import com.adferdv.ktile.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val SHOW_POLL_INTERVAL_MS = 100L
private const val FULLSCREEN_WAIT_TIMEOUT_MS = 2_000L

fun main() =
    application {
        val settingsCoroutineScope = rememberCoroutineScope()
        val settingsViewModel = remember { SettingsViewModel(settingsCoroutineScope) }
        var previewReady by remember { mutableStateOf(false) }

        Window(
            onCloseRequest = ::exitApplication,
            undecorated = true,
            transparent = true,
            title = FullscreenHelper.WINDOW_TITLE,
        ) {
            LaunchedEffect(Unit) {
                while (!window.isShowing) {
                    delay(SHOW_POLL_INTERVAL_MS.milliseconds)
                }
                FullscreenHelper.enterFullscreen(window, FULLSCREEN_WAIT_TIMEOUT_MS)
                previewReady = true
            }
            if (previewReady) {
                LayoutPreviewScreen(settingsViewModel)
            }
        }
    }
