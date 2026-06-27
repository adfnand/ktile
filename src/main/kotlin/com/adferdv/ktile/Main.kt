package com.adferdv.ktile

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.adferdv.ktile.ui.App

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KTile",
        ) {
            App()
        }
    }
