package com.adferdv.ktile.core.screen

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.awt.Frame

class AwtFullscreenStrategyTest {

    @Test
    fun setFullscreenMakesWindowFullscreen() {
        runBlocking {
            val frame = Frame()
            frame.addNotify()
            try {
                AwtFullscreenStrategy.setFullscreen(frame)

                val device = frame.graphicsConfiguration.device
                device.fullScreenWindow shouldBe frame
            } finally {
                frame.dispose()
            }
        }
    }

    @Test
    fun setFullscreenHandlesNonDisplayableWindowWithoutCrashing() {
        runBlocking {
            val frame = Frame()

            AwtFullscreenStrategy.setFullscreen(frame)

            frame.dispose()
        }
    }
}
