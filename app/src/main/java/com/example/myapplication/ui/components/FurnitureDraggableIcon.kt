package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
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
    onLongClick: () -> Unit,
    noAnimations: Boolean
) {
    var offsetX by remember { mutableFloatStateOf(furnitureUiItem.xPosition) }
    var offsetY by remember { mutableFloatStateOf(furnitureUiItem.yPosition) }

    LaunchedEffect(furnitureUiItem.xPosition, furnitureUiItem.yPosition) {
        offsetX = furnitureUiItem.xPosition
        offsetY = furnitureUiItem.yPosition
    }

    key(furnitureUiItem) {
        Card(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(furnitureUiItem.id) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x / scale
                            offsetY += dragAmount.y / scale
                        },
                        onDragEnd = {
                            viewModel.updateFurniturePosition(
                                furnitureUiItem.id,
                                offsetX,
                                offsetY
                            )
                        }
                    )
                }
                .combinedClickable(
                    onClick = { /* Furniture might not have a default click action */ },
                    onLongClick = onLongClick
                )
                .width(furnitureUiItem.displayWidth)
                .height(furnitureUiItem.displayHeight)
                .border(
                    BorderStroke(
                        furnitureUiItem.displayOutlineThickness,
                        furnitureUiItem.displayOutlineColor
                    )
                ),
            colors = CardDefaults.cardColors(containerColor = furnitureUiItem.displayBackgroundColor),
            elevation = if (noAnimations) CardDefaults.cardElevation(defaultElevation = 0.dp) else CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(4.dp)
            ) {
                Text(
                    text = furnitureUiItem.name,
                    color = furnitureUiItem.displayTextColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}