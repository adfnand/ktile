package com.adferdv.ktile.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.window.Window
import com.adferdv.ktile.core.screen.FullscreenHelper
import com.adferdv.ktile.core.screen.isLinux
import com.adferdv.ktile.core.screen.skipTaskbarX11
import com.adferdv.ktile.core.screen.workAreaBounds
import com.adferdv.ktile.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val SHOW_POLL_INTERVAL_MS = 100L
private const val FULLSCREEN_WAIT_TIMEOUT_MS = 2_000L
private const val FADE_IN_MS = 100
private const val FADE_OUT_MS = 60

@Composable
fun KTileWindow(
    visible: Boolean,
    onClose: () -> Unit,
    viewModel: SettingsViewModel,
) {
    var composeWindow by remember { mutableStateOf<ComposeWindow?>(null) }
    var previewReady by remember { mutableStateOf(false) }

    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(previewReady) {
        if (previewReady) {
            alphaAnim.snapTo(0f)
            alphaAnim.animateTo(1f, tween(durationMillis = FADE_IN_MS))
        } else {
            alphaAnim.animateTo(0f, tween(durationMillis = FADE_OUT_MS))
        }
    }

    Window(
        visible = true,
        onCloseRequest = onClose,
        undecorated = true,
        transparent = true,
        title = FullscreenHelper.WINDOW_TITLE,
    ) {
        LaunchedEffect(Unit) {
            composeWindow = window

            if (!isLinux()) {
                window.skipTaskbarX11()
            }

            window.opacity = 0f
            window.isVisible = false
            window.setBounds(window.workAreaBounds())
            window.setFocusableWindowState(false)
        }

        Box(
            modifier = Modifier.fillMaxSize().alpha(alphaAnim.value),
        ) {
            LayoutPreviewScreen(viewModel)
        }
    }

    LaunchedEffect(visible, composeWindow) {
        val win = composeWindow ?: return@LaunchedEffect

        if (!visible) {
            previewReady = false
            delay(FADE_OUT_MS.toLong().milliseconds)
            win.opacity = 0f
            win.focusableWindowState = false
            win.isVisible = false
            return@LaunchedEffect
        }

        win.bounds = win.workAreaBounds()
        win.opacity = 0f
        win.isVisible = true
        while (!win.isShowing) {
            delay(SHOW_POLL_INTERVAL_MS.milliseconds)
        }
        win.focusableWindowState = true
        win.toFront()
        win.requestFocus()
        win.opacity = 1f
        previewReady = true

        launch {
            FullscreenHelper.enterFullscreen(win, FULLSCREEN_WAIT_TIMEOUT_MS)
        }
    }
}
