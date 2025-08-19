package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.viewmodel.SeatingChartViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentDraggableIcon(
    studentUiItem: StudentUiItem,
    viewModel: SeatingChartViewModel,
    showBehavior: Boolean,
    scale: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    noAnimations: Boolean
) {
    var offsetX by remember { mutableFloatStateOf(studentUiItem.xPosition.toFloat()) }
    var offsetY by remember { mutableFloatStateOf(studentUiItem.yPosition.toFloat()) }

    LaunchedEffect(studentUiItem.xPosition, studentUiItem.yPosition) {
        offsetX = studentUiItem.xPosition.toFloat()
        offsetY = studentUiItem.yPosition.toFloat()
    }

    key(studentUiItem) {
        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(studentUiItem.id) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x / scale
                            offsetY += dragAmount.y / scale
                        },
                        onDragEnd = {
                            viewModel.updateStudentPosition(
                                studentUiItem.id,
                                studentUiItem.xPosition.toFloat(),
                                studentUiItem.yPosition.toFloat(),
                                offsetX,
                                offsetY
                            )
                        }
                    )
                }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .width(studentUiItem.displayWidth)
                .height(studentUiItem.displayHeight)
                .border(
                    BorderStroke(
                        if (isSelected) 4.dp else studentUiItem.displayOutlineThickness,
                        if (isSelected) MaterialTheme.colorScheme.primary else studentUiItem.displayOutlineColor
                    )
                ),
            colors = CardDefaults.cardColors(containerColor = studentUiItem.displayBackgroundColor),
            elevation = if (noAnimations) CardDefaults.cardElevation(defaultElevation = 0.dp) else CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(8.dp)
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
                                text = studentUiItem.recentBehaviorDescription.joinToString("\n"),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                color = studentUiItem.displayTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (studentUiItem.recentHomeworkDescription.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = studentUiItem.recentHomeworkDescription.joinToString("\n"),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                color = studentUiItem.displayTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}