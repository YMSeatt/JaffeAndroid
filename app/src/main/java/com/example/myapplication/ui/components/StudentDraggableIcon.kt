package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentDraggableIcon(
    studentUiItem: StudentUiItem,
    viewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    showBehavior: Boolean,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (Offset) -> Unit,
    onResize: (Float, Float) -> Unit,
    noAnimations: Boolean
) {
    var offsetX by remember { mutableFloatStateOf(studentUiItem.xPosition.toFloat()) }
    var offsetY by remember { mutableFloatStateOf(studentUiItem.yPosition.toFloat()) }
    val editModeEnabled by settingsViewModel.editModeEnabled.collectAsState()
    val gridSnapEnabled by settingsViewModel.gridSnapEnabled.collectAsState()
    val gridSize by settingsViewModel.gridSize.collectAsState()
    var width by remember { mutableStateOf(studentUiItem.displayWidth) }
    var height by remember { mutableStateOf(studentUiItem.displayHeight) }


    LaunchedEffect(studentUiItem.xPosition, studentUiItem.yPosition) {
        offsetX = studentUiItem.xPosition.toFloat()
        offsetY = studentUiItem.yPosition.toFloat()
    }

    LaunchedEffect(studentUiItem.displayWidth, studentUiItem.displayHeight) {
        width = studentUiItem.displayWidth
        height = studentUiItem.displayHeight
    }

    key(studentUiItem) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .width(width)
                .height(height)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(studentUiItem.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x / scale
                                offsetY += dragAmount.y / scale
                            },
                            onDragEnd = {
                                val finalX = if (gridSnapEnabled) {
                                    (offsetX / gridSize).roundToInt() * gridSize
                                } else {
                                    offsetX.roundToInt()
                                }
                                val finalY = if (gridSnapEnabled) {
                                    (offsetY / gridSize).roundToInt() * gridSize
                                } else {
                                    offsetY.roundToInt()
                                }

                                viewModel.updateStudentPosition(
                                    studentUiItem.id,
                                    studentUiItem.xPosition.toFloat(),
                                    studentUiItem.yPosition.toFloat(),
                                    finalX.toFloat(),
                                    finalY.toFloat()
                                )
                            }
                        )
                    }
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick as (() -> Unit)?
                    )
                    .border(
                        BorderStroke(
                            if (isSelected) 4.dp else studentUiItem.displayOutlineThickness,
                            if (isSelected) MaterialTheme.colorScheme.primary else studentUiItem.displayOutlineColor
                        )
                    ),
                colors = CardDefaults.cardColors(containerColor = studentUiItem.displayBackgroundColor),
                elevation = if (noAnimations) CardDefaults.cardElevation(defaultElevation = 0.dp) else CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = studentUiItem.fullName,
                            color = studentUiItem.displayTextColor,
                            textAlign = TextAlign.Center
                        )
                        if (showBehavior) {
                            if (studentUiItem.recentBehaviorDescription.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.recentBehaviorDescription.joinToString(
                                        "\n"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studentUiItem.displayTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (studentUiItem.recentHomeworkDescription.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.recentHomeworkDescription.joinToString(
                                        "\n"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studentUiItem.displayTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (studentUiItem.sessionLogText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.sessionLogText.joinToString("\n"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = studentUiItem.displayTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            if (editModeEnabled && isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(Color.Gray, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures {
                                change, dragAmount ->
                                change.consume()
                                width += (dragAmount.x / scale).dp
                                height += (dragAmount.y / scale).dp
                                onResize(width.value, height.value)
                            }
                        }
                )
            }
        }
    }
}