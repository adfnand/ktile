package com.adferdv.ktile.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adferdv.ktile.viewmodel.LayoutViewModel

@Composable
fun LayoutScreen(viewModel: LayoutViewModel) {
    val layout by viewModel.layout

    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            text = "Layout: $layout",
            fontSize = 24.sp,
        )
        Button(
            onClick = { viewModel.loadLayout() },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Load")
        }
    }
}
