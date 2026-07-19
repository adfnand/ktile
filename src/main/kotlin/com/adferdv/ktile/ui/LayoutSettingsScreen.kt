import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LayoutSettingsScreen() {
    val columnWeights = remember { mutableStateListOf(1, 1, 1, 1) }
    val rowWeights = remember { mutableStateListOf(1, 1, 1) }
    val keyLabels =
        remember {
            mutableStateListOf(
                mutableStateListOf("Q", "W", "E", "R"),
                mutableStateListOf("A", "S", "D", "F"),
                mutableStateListOf("Z", "X", "C", "V"),
            )
        }
    val focusRequester = remember { FocusRequester() }
    var selectedPosition = remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var usedKeys = remember { mutableStateOf<Set<String>>(keyLabels.flatten().toSet()) }
    var showDialog = remember { mutableStateOf(false) }
    var dialogMessage = remember { mutableStateOf("") }

    fun clearSelectionIfHidden() {
        selectedPosition.value?.let { (row, col) ->
            if (rowWeights[row] == 0 || columnWeights[col] == 0) {
                selectedPosition.value = null
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        val displayChar = keyEvent.awtEventOrNull?.keyChar

                        if (displayChar != null) {
                            if (displayChar.isLetterOrDigit()) {
                                selectedPosition.value?.let { (row, col) ->
                                    if (row in keyLabels.indices &&
                                        col in keyLabels[row].indices
                                    ) {
                                        val oldKey = keyLabels[row][col]
                                        val newKey =
                                            if (displayChar.isLetter()) {
                                                displayChar.uppercase()
                                            } else {
                                                displayChar.toString()
                                            }

                                        if (usedKeys.value.contains(newKey)) {
                                            showDialog.value = true
                                            dialogMessage.value =
                                                "Selected key is already added to the layout.\n" +
                                                "Please, replace current position with another key and try again."
                                        } else {
                                            keyLabels[row][col] = newKey
                                            usedKeys.value = usedKeys.value - oldKey + newKey
                                            selectedPosition.value = null
                                        }

                                        return@onKeyEvent true
                                    }
                                }
                            }
                        }
                    }
                    false
                },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Text(
            text = "Column and row weights",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 20.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(modifier = Modifier.width(80.dp))
            columnWeights.forEachIndexed { colIndex, weight ->
                WeightControl(
                    value = weight,
                    onIncrement = {
                        columnWeights[colIndex] = (columnWeights[colIndex] + 1).coerceAtLeast(0)
                    },
                    onDecrement = {
                        val newVal = (columnWeights[colIndex] - 1).coerceAtLeast(0)
                        columnWeights[colIndex] = newVal
                        clearSelectionIfHidden()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        rowWeights.forEachIndexed { rowIndex, rowWeight ->
            val rowModifier =
                if (rowWeight > 0) {
                    Modifier.weight(rowWeight.toFloat())
                } else {
                    Modifier.height(48.dp)
                }

            Box(
                modifier = Modifier.fillMaxWidth().then(rowModifier),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WeightControl(
                        value = rowWeight,
                        onIncrement = {
                            rowWeights[rowIndex] = (rowWeights[rowIndex] + 1).coerceAtLeast(0)
                        },
                        onDecrement = {
                            val newVal = (rowWeights[rowIndex] - 1).coerceAtLeast(0)
                            rowWeights[rowIndex] = newVal
                            clearSelectionIfHidden()
                        },
                        modifier = Modifier.width(80.dp),
                    )

                    if (rowWeight > 0) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val rowLabels = keyLabels.getOrElse(rowIndex) { mutableStateListOf() }
                            columnWeights.forEachIndexed { colIndex, colWeight ->
                                if (colWeight > 0) {
                                    val label = rowLabels.getOrElse(colIndex) { "?" }
                                    KeyChip(
                                        label = label,
                                        isSelected = selectedPosition.value == rowIndex to colIndex,
                                        onClick = {
                                            selectedPosition.value =
                                                if (selectedPosition.value ==
                                                    rowIndex to colIndex
                                                ) {
                                                    null
                                                } else {
                                                    rowIndex to colIndex
                                                }
                                        },
                                        modifier =
                                            Modifier
                                                .weight(colWeight.toFloat())
                                                .fillMaxHeight(),
                                    )
                                }
                            }
                        }
                    }

                    if (showDialog.value == true) {
                        AlertDialog(
                            onDismissRequest = { showDialog.value = false },
                            title = { Text("Error") },
                            text = { Text(dialogMessage.value) },
                            confirmButton = {
                                TextButton(onClick = { showDialog.value = false }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeightControl(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = "−",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.size(28.dp).clickable(
                    enabled = enabled && value > 0,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onDecrement() },
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = "+",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.size(28.dp).clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onIncrement() },
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
fun KeyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val purple = Color(0xFF9C27B0)

    Surface(
        modifier =
            modifier
                .clickable { onClick() }
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colors.primary,
                            shape = RoundedCornerShape(4.dp),
                        )
                    } else {
                        Modifier
                    },
                ),
        color =
            if (isSelected) {
                purple.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colors.surface.copy(alpha = 0.5f)
            },
        shape = RoundedCornerShape(4.dp),
        elevation = 2.dp,
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
