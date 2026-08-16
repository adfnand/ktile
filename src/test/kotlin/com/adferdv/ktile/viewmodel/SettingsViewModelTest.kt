package com.adferdv.ktile.viewmodel

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
}
