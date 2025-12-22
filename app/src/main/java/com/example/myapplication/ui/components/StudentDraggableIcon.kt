package com.example.myapplication.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.util.getFontFamily
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
    canvasSize: androidx.compose.ui.unit.IntSize,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onResize: (Float, Float) -> Unit,
    noAnimations: Boolean,
    canvasScale: Float,
    canvasOffset: Offset
) {
    var offsetX by remember { mutableFloatStateOf(studentUiItem.xPosition.value) }
    var offsetY by remember { mutableFloatStateOf(studentUiItem.yPosition.value) }
    val editModeEnabled by settingsViewModel.editModeEnabled.collectAsState()
    val gridSnapEnabled by settingsViewModel.gridSnapEnabled.collectAsState()
    val gridSize by settingsViewModel.gridSize.collectAsState()
    val autoExpandEnabled by settingsViewModel.autoExpandStudentBoxes.collectAsState()
    var width by remember { mutableStateOf(studentUiItem.displayWidth.value) }
    var height by remember { mutableStateOf(studentUiItem.displayHeight.value) }
    var cardSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val density = LocalDensity.current


    // Boundary check removed to prevent unexpected jumping. 
    // The user should be able to place boxes freely on the canvas.

    LaunchedEffect(studentUiItem.xPosition.value, studentUiItem.yPosition.value) {
        offsetX = studentUiItem.xPosition.value
        offsetY = studentUiItem.yPosition.value
    }

    LaunchedEffect(studentUiItem.displayWidth.value, studentUiItem.displayHeight.value) {
        width = studentUiItem.displayWidth.value
        height = studentUiItem.displayHeight.value
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    key(studentUiItem) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = ((offsetX * canvasScale) + canvasOffset.x).roundToInt(),
                        y = ((offsetY * canvasScale) + canvasOffset.y).roundToInt()
                    )
                }
                .scale(scale)
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

                                // Update MutableState for instant UI feedback to observers
                                studentUiItem.xPosition.value = offsetX
                                studentUiItem.yPosition.value = offsetY
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
                                    studentUiItem.xPosition.value,
                                    studentUiItem.yPosition.value,
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
                                .width(studentUiItem.displayWidth.value)
                                .heightIn(min = studentUiItem.displayHeight.value, max = 300.dp)
                        }
                    ),
                shape = RoundedCornerShape(studentUiItem.displayCornerRadius.value),
                colors = CardDefaults.cardColors(
                    containerColor = studentUiItem.displayBackgroundColor.value.first()
                ),
                border = BorderStroke(
                    if (isSelected) 6.dp else studentUiItem.displayOutlineThickness.value,
                    if (isSelected) MaterialTheme.colorScheme.primary else studentUiItem.displayOutlineColor.value.first()
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else 2.dp
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(studentUiItem.displayPadding.value)
                ) {
                    val backgroundColors = studentUiItem.displayBackgroundColor.value
                    if (backgroundColors.size > 1) {
                        Box(modifier = Modifier.matchParentSize()) {
                            backgroundColors.forEachIndexed { index, color ->
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .padding( (index * 4).dp)
                                        .background(color, shape = RoundedCornerShape(studentUiItem.displayCornerRadius.value - (index * 4).dp))
                                )
                            }
                        }
                    }

                    val outlineColors = studentUiItem.displayOutlineColor.value
                    if (outlineColors.size > 1) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val strokeWidth = (if (isSelected) 4.dp else studentUiItem.displayOutlineThickness.value).toPx()
                            val segmentLength = 10.dp.toPx()
                            outlineColors.forEachIndexed { index, color ->
                                drawRoundRect(
                                    color = color,
                                    style = Stroke(
                                        width = strokeWidth,
                                        pathEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(segmentLength, segmentLength),
                                            phase = segmentLength / outlineColors.size * index
                                        )
                                    )
                                )
                            }
                        }
                    }
                    if (studentUiItem.temporaryTask != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "Assigned Task",
                            modifier = Modifier.align(Alignment.TopEnd).size(16.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val nameParts = studentUiItem.fullName.split(" ")
                        val firstName = nameParts.firstOrNull() ?: ""
                        val lastName = if (nameParts.size > 1) nameParts.last() else ""
                        Text(
                            text = firstName,
                            style = TextStyle(
                                color = studentUiItem.fontColor.value,
                                fontFamily = getFontFamily(studentUiItem.fontFamily.value),
                                fontSize = studentUiItem.fontSize.value.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = lastName,
                            style = TextStyle(
                                color = studentUiItem.fontColor.value,
                                fontFamily = getFontFamily(studentUiItem.fontFamily.value),
                                fontSize = studentUiItem.fontSize.value.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        if (showBehavior) {
                            val behaviorLogs = studentUiItem.recentBehaviorDescription
                            val quizLogs = studentUiItem.recentQuizDescription
                            val homeworkLogs = studentUiItem.recentHomeworkDescription
                            val sessionLogs = studentUiItem.sessionLogText

                            if (behaviorLogs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                behaviorLogs.forEach { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = studentUiItem.fontColor.value,
                                            fontFamily = getFontFamily(studentUiItem.fontFamily.value),
                                            fontSize = studentUiItem.fontSize.value.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }

                            if (quizLogs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                quizLogs.forEach { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF006400), // Dark Green
                                            fontFamily = getFontFamily(studentUiItem.fontFamily.value),
                                            fontSize = studentUiItem.fontSize.value.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            if ((behaviorLogs.isNotEmpty() || quizLogs.isNotEmpty()) && homeworkLogs.isNotEmpty()) {
                                Text(
                                    text = "--- Homework ---",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray,
                                        fontFamily = getFontFamily(studentUiItem.fontFamily.value),
                                        fontSize = (studentUiItem.fontSize.value - 2).sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }

                            if (homeworkLogs.isNotEmpty()) {
                                homeworkLogs.forEach { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF800080), // Purple
                                            fontFamily = getFontFamily(studentUiItem.fontFamily.value),
                                            fontSize = studentUiItem.fontSize.value.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            if (sessionLogs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                sessionLogs.forEach { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = studentUiItem.fontColor.value,
                                            fontFamily = getFontFamily(studentUiItem.fontFamily.value),
                                            fontSize = studentUiItem.fontSize.value.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
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
