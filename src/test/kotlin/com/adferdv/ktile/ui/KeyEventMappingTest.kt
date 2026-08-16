package com.adferdv.ktile.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class KeyEventMappingTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `letter key event maps to uppercase letter`() {
        val captured = captureKeyEvent { keyDown(androidx.compose.ui.input.key.Key.P) }

        getDisplayCharFromKeyEvent(captured) shouldBe "P"
    }

    @Test
    fun `digit key event maps to digit`() {
        val captured = captureKeyEvent { keyDown(androidx.compose.ui.input.key.Key.Eight) }

        getDisplayCharFromKeyEvent(captured) shouldBe "8"
    }

    private fun captureKeyEvent(dispatch: androidx.compose.ui.test.KeyInjectionScope.() -> Unit): KeyEvent {
        var captured: KeyEvent? = null
        composeTestRule.setContent {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .focusable()
                        .onPreviewKeyEvent { event ->
                            captured = event
                            true
                        },
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().performKeyInput(dispatch)
        composeTestRule.waitForIdle()

        return captured ?: error("No key event captured")
    }
}
