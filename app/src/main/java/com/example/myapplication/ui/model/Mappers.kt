package com.example.myapplication.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Furniture
import com.example.myapplication.util.safeParseColor

/**
 * Creates a new [FurnitureUiItem] from a [Furniture] entity.
 */
fun Furniture.toFurnitureUiItem(): FurnitureUiItem {
    return FurnitureUiItem(
        id = this.id,
        stringId = this.stringId,
        name = mutableStateOf(this.name),
        type = mutableStateOf(this.type),
        xPosition = mutableStateOf(this.xPosition),
        yPosition = mutableStateOf(this.yPosition),
        displayWidth = mutableStateOf(this.width.dp),
        displayHeight = mutableStateOf(this.height.dp),
        displayBackgroundColor = mutableStateOf(this.fillColor?.let { safeParseColor(it) } ?: Color.LightGray),
        displayOutlineColor = mutableStateOf(this.outlineColor?.let { safeParseColor(it) } ?: Color.Black),
        displayTextColor = mutableStateOf(Color.Black),
        displayOutlineThickness = mutableStateOf(1.dp)
    )
}

/**
 * Updates an existing [FurnitureUiItem] with fresh data from a [Furniture] entity.
 * Performs differential updates on [MutableState] fields to minimize recomposition.
 */
fun Furniture.updateFurnitureUiItem(item: FurnitureUiItem) {
    updateIfChanged(item.name, this.name)
    updateIfChanged(item.type, this.type)
    updateIfChanged(item.xPosition, this.xPosition)
    updateIfChanged(item.yPosition, this.yPosition)
    updateIfChanged(item.displayWidth, this.width.dp)
    updateIfChanged(item.displayHeight, this.height.dp)
    updateIfChanged(item.displayBackgroundColor, this.fillColor?.let { safeParseColor(it) } ?: Color.LightGray)
    updateIfChanged(item.displayOutlineColor, this.outlineColor?.let { safeParseColor(it) } ?: Color.Black)
}

/**
 * Helper to update a [MutableState] only if the new value differs from the current one.
 */
private fun <T> updateIfChanged(state: MutableState<T>, newValue: T) {
    if (state.value != newValue) {
        state.value = newValue
    }
}
