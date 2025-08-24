package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.Student



import com.example.myapplication.utils.safeParseColor

fun Furniture.toUiItem(): FurnitureUiItem {
    return FurnitureUiItem(
        id = this.id,
        stringId = this.stringId,
        name = this.name,
        type = this.type,
        xPosition = this.xPosition,
        yPosition = this.yPosition,
        displayWidth = this.width.dp,
        displayHeight = this.height.dp,
        displayBackgroundColor = this.fillColor?.let { safeParseColor(it) } ?: Color.LightGray,
        displayOutlineColor = this.outlineColor?.let { safeParseColor(it) } ?: Color.Black,
        displayTextColor = Color.Black, // Assuming a default value
        displayOutlineThickness = 1.dp // Assuming a default value
    )
}
