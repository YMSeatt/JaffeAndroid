package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.utils.getFontFamily
import com.example.myapplication.viewmodel.SeatingChartViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Icon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentDraggableIcon(
    studentUiItem: StudentUiItem,
    viewModel: SeatingChartViewModel,
    settingsViewModel: SettingsViewModel,
    showBehavior: Boolean,
    canvasSize: androidx.compose.ui.unit.IntSize,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onResize: (Float, Float) -> Unit,
    noAnimations: Boolean,
    canvasScale: Float,
    canvasOffset: Offset
) {
    var offsetX by remember { mutableFloatStateOf(studentUiItem.xPosition.toFloat()) }
    var offsetY by remember { mutableFloatStateOf(studentUiItem.yPosition.toFloat()) }
    val editModeEnabled by settingsViewModel.editModeEnabled.collectAsState()
    val gridSnapEnabled by settingsViewModel.gridSnapEnabled.collectAsState()
    val gridSize by settingsViewModel.gridSize.collectAsState()
    val autoExpandEnabled by settingsViewModel.autoExpandStudentBoxes.collectAsState()
    var width by remember { mutableStateOf(studentUiItem.displayWidth) }
    var height by remember { mutableStateOf(studentUiItem.displayHeight) }
    var cardSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val density = LocalDensity.current


    LaunchedEffect(cardSize, canvasSize) {
        if (cardSize == androidx.compose.ui.unit.IntSize.Zero || canvasSize == androidx.compose.ui.unit.IntSize.Zero) return@LaunchedEffect

        val studentBoxBottom = offsetY + cardSize.height
        val canvasBottom = canvasSize.height

        if (studentBoxBottom > canvasBottom) {
            val newOffsetY = (canvasBottom - cardSize.height).toFloat()
            if (newOffsetY.roundToInt() != offsetY.roundToInt()) {
                viewModel.updateStudentPosition(
                    studentUiItem.id,
                    studentUiItem.xPosition.toFloat(),
                    studentUiItem.yPosition.toFloat(),
                    offsetX,
                    newOffsetY
                )
            }
        }
    }

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
                .offset {
                    IntOffset(
                        x = ((offsetX * canvasScale) + canvasOffset.x).roundToInt(),
                        y = ((offsetY * canvasScale) + canvasOffset.y).roundToInt()
                    )
                }
        ) {
            Card(
                modifier = Modifier
                    .onSizeChanged { cardSize = it }
                    .pointerInput(studentUiItem.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x / canvasScale
                                offsetY += dragAmount.y / canvasScale
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
                        onLongClick = onLongClick
                    )
                    .then(
                        if (!autoExpandEnabled) {
                            Modifier.width(width).height(height)
                        } else {
                            Modifier
                                .width(studentUiItem.displayWidth)
                                .heightIn(min = studentUiItem.displayHeight, max = 300.dp)
                        }
                    ),
                shape = RoundedCornerShape(studentUiItem.displayCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = studentUiItem.displayBackgroundColor.first()
                ),
                border = BorderStroke(
                    if (isSelected) 4.dp else studentUiItem.displayOutlineThickness,
                    if (isSelected) MaterialTheme.colorScheme.primary else studentUiItem.displayOutlineColor.first()
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(studentUiItem.displayPadding)
                ) {
                    if (studentUiItem.temporaryTask != null) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Assigned Task",
                            modifier = Modifier.align(Alignment.TopEnd).size(16.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val nameParts = studentUiItem.fullName.split(" ")
                        val firstName = nameParts.firstOrNull() ?: ""
                        val lastName = if (nameParts.size > 1) nameParts.last() else ""
                        Text(
                            text = firstName,
                            style = TextStyle(
                                color = studentUiItem.fontColor,
                                fontFamily = getFontFamily(studentUiItem.fontFamily),
                                fontSize = studentUiItem.fontSize.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = lastName,
                            style = TextStyle(
                                color = studentUiItem.fontColor,
                                fontFamily = getFontFamily(studentUiItem.fontFamily),
                                fontSize = studentUiItem.fontSize.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        if (showBehavior) {
                            if (studentUiItem.recentBehaviorDescription.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.recentBehaviorDescription.joinToString("\n"),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = studentUiItem.fontColor,
                                        fontFamily = getFontFamily(studentUiItem.fontFamily),
                                        fontSize = studentUiItem.fontSize.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                            if (studentUiItem.recentHomeworkDescription.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.recentHomeworkDescription.joinToString("\n"),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = studentUiItem.fontColor,
                                        fontFamily = getFontFamily(studentUiItem.fontFamily),
                                        fontSize = studentUiItem.fontSize.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                            if (studentUiItem.recentQuizDescription.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.recentQuizDescription.joinToString("\n"),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = studentUiItem.fontColor,
                                        fontFamily = getFontFamily(studentUiItem.fontFamily),
                                        fontSize = studentUiItem.fontSize.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                            if (studentUiItem.sessionLogText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.sessionLogText.joinToString("\n"),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = studentUiItem.fontColor,
                                        fontFamily = getFontFamily(studentUiItem.fontFamily),
                                        fontSize = studentUiItem.fontSize.sp,
                                        textAlign = TextAlign.Center
                                    )
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
                                with(density) {
                                    width += (dragAmount.x / canvasScale).toDp()
                                    height += (dragAmount.y / canvasScale).toDp()
                                }
                                onResize(width.value, height.value)
                            }
                        }
                )
            }
        }
    }
}
