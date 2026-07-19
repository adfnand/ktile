package com.adferdv.ktile.ui

import LayoutSettingsScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adferdv.ktile.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
    ) {
        Text(
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        LayoutSettingsScreen()
    }
}
