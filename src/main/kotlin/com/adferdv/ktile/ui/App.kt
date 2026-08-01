package com.adferdv.ktile.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.adferdv.ktile.viewmodel.SettingsViewModel

@Composable
fun App(settingsViewModel: SettingsViewModel) {
    MaterialTheme {
        // SettingsScreen(settingsViewModel)
        LayoutPreviewScreen(settingsViewModel)
    }
}
