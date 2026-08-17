package com.adferdv.ktile.viewmodel

import com.adferdv.ktile.core.hotkey.Hotkey
import com.adferdv.ktile.core.hotkey.ModifierKey
import com.adferdv.ktile.core.hotkey.toDisplayString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class SettingsViewModelTest {
    @Test
    fun `default layout settings hold default bindings`() {
        val viewModel = SettingsViewModel(CoroutineScope(Dispatchers.Unconfined))

        val settings = viewModel.layoutSettings
        settings.columnWeights.toList() shouldBe listOf(1, 1, 1, 1)
        settings.rowWeights.toList() shouldBe listOf(1, 1, 1)
        settings.keyLabels.map { it.toList() } shouldBe
            listOf(
                listOf("Q", "W", "E", "R"),
                listOf("A", "S", "D", "F"),
                listOf("Z", "X", "C", "V"),
            )
    }

    @Test
    fun `default layout settings expose default helper`() {
        val settings = LayoutSettings.default()

        settings.columnWeights.toList() shouldBe listOf(1, 1, 1, 1)
        settings.rowWeights.toList() shouldBe listOf(1, 1, 1)
        settings.keyLabels.map { it.toList() } shouldBe
            listOf(
                listOf("Q", "W", "E", "R"),
                listOf("A", "S", "D", "F"),
                listOf("Z", "X", "C", "V"),
            )
    }

    @Test
    fun `default toggle hotkey is Super plus K`() {
        val viewModel = SettingsViewModel(CoroutineScope(Dispatchers.Unconfined))

        viewModel.toggleHotkey shouldBe Hotkey.DEFAULT_TOGGLE
        viewModel.toggleHotkey.toDisplayString() shouldBe "Super+K"
    }

    @Test
    fun `toggle hotkey can be updated`() {
        val viewModel = SettingsViewModel(CoroutineScope(Dispatchers.Unconfined))

        val newHotkey = Hotkey(10, setOf(ModifierKey.CTRL, ModifierKey.SHIFT))
        viewModel.toggleHotkey = newHotkey

        viewModel.toggleHotkey shouldBe newHotkey
    }
}
