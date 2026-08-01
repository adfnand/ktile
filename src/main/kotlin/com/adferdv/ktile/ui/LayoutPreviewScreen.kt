package com.adferdv.ktile.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adferdv.ktile.viewmodel.SettingsViewModel

@Composable
fun LayoutPreviewScreen(viewModel: SettingsViewModel) {
    val layout = viewModel.layoutSettings

    Column(
        modifier = Modifier.fillMaxSize().testTag("layout-preview"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        layout.rowWeights.forEachIndexed { rowIndex, rowWeight ->
            if (rowWeight > 0) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(rowWeight.toFloat()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    layout.columnWeights.forEachIndexed { colIndex, colWeight ->
                        if (colWeight > 0) {
                            PreviewCell(
                                label = layout.keyLabels[rowIndex].getOrElse(colIndex) { "?" },
                                modifier =
                                    Modifier
                                        .weight(colWeight.toFloat())
                                        .fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewCell(
    label: String,
    modifier: Modifier = Modifier,
) {
    val purple = Color(PURPLE_COLOR)

    Box(
        modifier =
            modifier
                .border(
                    width = 2.dp,
                    color = purple,
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = purple,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
