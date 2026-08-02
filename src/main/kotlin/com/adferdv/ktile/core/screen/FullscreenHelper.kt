package com.adferdv.ktile.core.screen

import java.awt.Window

object FullscreenHelper {
    const val WINDOW_TITLE = "KTile Preview"

    private val strategy: FullscreenStrategy by lazy { resolveStrategy() }

    private fun resolveStrategy(): FullscreenStrategy =
        if (System.getProperty("os.name").startsWith("Mac")) {
            AwtFullscreenStrategy
        } else {
            X11FullscreenStrategy
        }

    suspend fun enterFullscreen(
        window: Window,
        timeoutMs: Long,
    ) {
        strategy.setFullscreen(window)
        strategy.waitForFullscreen(window, timeoutMs)
    }
}