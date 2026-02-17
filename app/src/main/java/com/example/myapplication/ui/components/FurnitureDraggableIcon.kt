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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.model.FurnitureUiItem
import com.example.myapplication.viewmodel.SeatingChartViewModel
import kotlin.math.roundToInt

/**
 * A draggable and interactive UI component representing furniture on the seating chart.
 *
 * This component utilizes the "Fluid Interaction" model:
 * 1. **Direct State Observation**: Binds UI properties directly to [MutableState] fields
 *    in the cached [FurnitureUiItem], ensuring high-performance updates.
 * 2. **Optimistic Feedback**: Updates position and size states immediately during gestures
 *    for 60fps responsiveness, while deferring database synchronization until the gesture ends.
 * 3. **Minimal Recomposition**: Using [key] with the item ID and [MutableState] properties
 *    allows Compose to update only the modified parts of the UI.
 */
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
    key(furnitureUiItem.id) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = ((furnitureUiItem.xPosition.value * scale) + canvasOffset.x).roundToInt(),
                        y = ((furnitureUiItem.yPosition.value * scale) + canvasOffset.y).roundToInt()
                    )
                }
                .width(furnitureUiItem.displayWidth.value)
                .height(furnitureUiItem.displayHeight.value)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(furnitureUiItem.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Update MutableState directly for optimistic feedback
                                furnitureUiItem.xPosition.value += dragAmount.x / scale
                                furnitureUiItem.yPosition.value += dragAmount.y / scale
                            },
                            onDragEnd = {
                                val finalX = if (gridSnapEnabled) {
                                    (furnitureUiItem.xPosition.value / gridSize).roundToInt() * gridSize.toFloat()
                                } else {
                                    furnitureUiItem.xPosition.value
                                }
                                val finalY = if (gridSnapEnabled) {
                                    (furnitureUiItem.yPosition.value / gridSize).roundToInt() * gridSize.toFloat()
                                } else {
                                    furnitureUiItem.yPosition.value
                                }
                                viewModel.updateFurniturePosition(
                                    furnitureUiItem.id,
                                    finalX,
                                    finalY
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
                            if (isSelected) 4.dp else furnitureUiItem.displayOutlineThickness.value,
                            if (isSelected) MaterialTheme.colorScheme.primary else furnitureUiItem.displayOutlineColor.value
                        )
                    ),
                colors = CardDefaults.cardColors(containerColor = furnitureUiItem.displayBackgroundColor.value),
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
                        text = furnitureUiItem.name.value,
                        color = furnitureUiItem.displayTextColor.value,
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
                                furnitureUiItem.displayWidth.value += (dragAmount.x / scale).dp
                                furnitureUiItem.displayHeight.value += (dragAmount.y / scale).dp
                                onResize(furnitureUiItem.displayWidth.value.value, furnitureUiItem.displayHeight.value.value)
                            }
                        }
                )
            }
        }
    }
}
