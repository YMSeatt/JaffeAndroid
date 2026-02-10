package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.FurnitureUiItem
import com.example.myapplication.viewmodel.SeatingChartViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FurnitureDraggableIcon(
    furnitureUiItem: FurnitureUiItem,
    viewModel: SeatingChartViewModel,
    scale: Float,
    canvasOffset: androidx.compose.ui.geometry.Offset,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit,
    onResize: (Float, Float) -> Unit,
    noAnimations: Boolean,
    editModeEnabled: Boolean,
    gridSnapEnabled: Boolean,
    gridSize: Int
) {
    var offsetX by remember { mutableFloatStateOf(furnitureUiItem.xPosition) }
    var offsetY by remember { mutableFloatStateOf(furnitureUiItem.yPosition) }
    var width by remember { mutableStateOf(furnitureUiItem.displayWidth) }
    var height by remember { mutableStateOf(furnitureUiItem.displayHeight) }

    LaunchedEffect(furnitureUiItem.xPosition, furnitureUiItem.yPosition) {
        offsetX = furnitureUiItem.xPosition
        offsetY = furnitureUiItem.yPosition
    }

    LaunchedEffect(furnitureUiItem.displayWidth, furnitureUiItem.displayHeight) {
        width = furnitureUiItem.displayWidth
        height = furnitureUiItem.displayHeight
    }

    key(furnitureUiItem) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = ((offsetX * scale) + canvasOffset.x).roundToInt(),
                        y = ((offsetY * scale) + canvasOffset.y).roundToInt()
                    )
                }
                .width(width)
                .height(height)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(furnitureUiItem.id) {
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
                                    offsetX
                                }
                                val finalY = if (gridSnapEnabled) {
                                    (offsetY / gridSize).roundToInt() * gridSize
                                } else {
                                    offsetY
                                }
                                viewModel.updateFurniturePosition(
                                    furnitureUiItem.id,
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
                            if (isSelected) 4.dp else furnitureUiItem.displayOutlineThickness,
                            if (isSelected) MaterialTheme.colorScheme.primary else furnitureUiItem.displayOutlineColor
                        )
                    ),
                colors = CardDefaults.cardColors(containerColor = furnitureUiItem.displayBackgroundColor),
                elevation = if (noAnimations) CardDefaults.cardElevation(defaultElevation = 0.dp) else CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    Text(
                        text = furnitureUiItem.name,
                        color = furnitureUiItem.displayTextColor,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (editModeEnabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(Color.Gray, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
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