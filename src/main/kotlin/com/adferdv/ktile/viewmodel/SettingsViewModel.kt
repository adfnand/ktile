package com.adferdv.ktile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope

class SettingsViewModel(
    private val coroutineScope: CoroutineScope,
) {
    var layoutSettings: LayoutSettings by mutableStateOf(LayoutSettings.default())
}

data class LayoutSettings(
    val columnWeights: SnapshotStateList<Int>,
    val rowWeights: SnapshotStateList<Int>,
    val keyLabels: SnapshotStateList<SnapshotStateList<String>>,
) {
    companion object {
        fun default() =
            LayoutSettings(
                columnWeights = mutableStateListOf(1, 1, 1, 1),
                rowWeights = mutableStateListOf(1, 1, 1),
                keyLabels =
                    mutableStateListOf(
                        mutableStateListOf("Q", "W", "E", "R"),
                        mutableStateListOf("A", "S", "D", "F"),
                        mutableStateListOf("Z", "X", "C", "V"),
                    ),
            )
    }
}
