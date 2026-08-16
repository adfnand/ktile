package com.adferdv.ktile.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray

@Composable
fun ApplicationScope.KTileTray(
    icon: Painter,
    onToggle: () -> Unit,
) {
    Tray(
        icon = icon,
        tooltip = "KTile",
        onAction = onToggle,
        menu = {
            Item("Toggle KTile", onClick = onToggle)
            Item("Quit", onClick = ::exitApplication)
        },
    )
}
