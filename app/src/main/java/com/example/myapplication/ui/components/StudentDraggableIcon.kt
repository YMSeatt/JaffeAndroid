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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.utils.getFontFamily
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
    canvasSize: androidx.compose.ui.unit.IntSize,
    canvasOffset: androidx.compose.ui.geometry.Offset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onResize: (Float, Float) -> Unit,
    noAnimations: Boolean
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


    LaunchedEffect(cardSize, canvasSize, canvasOffset, scale) {
        if (cardSize == androidx.compose.ui.unit.IntSize.Zero || canvasSize == androidx.compose.ui.unit.IntSize.Zero) return@LaunchedEffect

        val scaledCardHeight = cardSize.height * scale
        val studentBoxScreenY = (offsetY * scale) + canvasOffset.y
        val canvasBottom = canvasSize.height

        if (studentBoxScreenY + scaledCardHeight > canvasBottom) {
            val newOffsetY = (canvasBottom - scaledCardHeight - canvasOffset.y) / scale
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
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = canvasOffset.x,
                    translationY = canvasOffset.y
                )
        ) {
            Card(
                modifier = Modifier
                    .onSizeChanged { cardSize = it }
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
                        onLongClick = onLongClick
                    )
                    .border(
                        BorderStroke(
                            if (isSelected) 4.dp else studentUiItem.displayOutlineThickness,
                            if (isSelected) MaterialTheme.colorScheme.primary else studentUiItem.displayOutlineColor
                        )
                    )
                    .then(
                        if (!autoExpandEnabled) {
                            Modifier.width(width).height(height)
                        } else {
                            val calculatedHeight = calculateStudentIconHeight(
                                defaultHeight = studentUiItem.displayHeight,
                                showBehavior = showBehavior,
                                behaviorText = studentUiItem.recentBehaviorDescription.joinToString("\n"),
                                homeworkText = studentUiItem.recentHomeworkDescription.joinToString("\n"),
                                sessionLogText = studentUiItem.sessionLogText.joinToString("\n"),
                                lineHeight = with(LocalDensity.current) { MaterialTheme.typography.bodySmall.lineHeight.toDp() }
                            )
                            Modifier
                                .width(studentUiItem.displayWidth) // Always respect minimum width
                                .heightIn(min = studentUiItem.displayHeight) // Respect minimum height, and expand if content is larger
                        }
                    ),
                shape = RoundedCornerShape(studentUiItem.displayCornerRadius),
                colors = CardDefaults.cardColors(containerColor = studentUiItem.displayBackgroundColor),
                elevation = if (noAnimations) CardDefaults.cardElevation(defaultElevation = 0.dp) else CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(studentUiItem.displayPadding)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = studentUiItem.fullName,
                            color = studentUiItem.displayTextColor,
                            textAlign = TextAlign.Center,
                            fontFamily = getFontFamily(studentUiItem.fontFamily),
                            fontSize = studentUiItem.fontSize.sp
                        )
                        if (showBehavior) {
                            if (studentUiItem.recentBehaviorDescription.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.recentBehaviorDescription.joinToString(
                                        "\n"
                                    ),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = getFontFamily(studentUiItem.fontFamily),
                                        fontSize = studentUiItem.fontSize.sp
                                    ),
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
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = getFontFamily(studentUiItem.fontFamily),
                                        fontSize = studentUiItem.fontSize.sp
                                    ),
                                    color = studentUiItem.displayTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (studentUiItem.sessionLogText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = studentUiItem.sessionLogText.joinToString("\n"),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = getFontFamily(studentUiItem.fontFamily),
                                        fontSize = studentUiItem.fontSize.sp
                                    ),
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
                                with(density) {
                                    width += (dragAmount.x / scale).toDp()
                                    height += (dragAmount.y / scale).toDp()
                                }
                                onResize(width.value, height.value)
                            }
                        }
                )
            }
        }
    }
}