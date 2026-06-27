package com.adferdv.ktile.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.adferdv.ktile.service.LayoutService
import com.adferdv.ktile.viewmodel.LayoutViewModel

@Composable
fun App() {
    val service = remember { LayoutService() }
    val scope = rememberCoroutineScope()
    val viewModel = remember { LayoutViewModel(service, scope) }

    MaterialTheme {
        LayoutScreen(viewModel)
    }
}
