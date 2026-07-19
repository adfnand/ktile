package com.adferdv.ktile.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.adferdv.ktile.viewmodel.SettingsViewModel

@Composable
fun App() {
    val settingsCoroutineScope = rememberCoroutineScope()
    val settingsViewModel = remember { SettingsViewModel(settingsCoroutineScope) }

    MaterialTheme {
        SettingsScreen(settingsViewModel)
    }
}
