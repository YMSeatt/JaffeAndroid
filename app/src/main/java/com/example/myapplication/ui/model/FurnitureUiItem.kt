package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class FurnitureUiItem(
    val id: Int,
    val stringId: String?,
    val name: String,
    val type: String,
    var xPosition: Float,
    var yPosition: Float,
    val displayWidth: Dp,
    val displayHeight: Dp,
    val displayBackgroundColor: Color,
    val displayOutlineColor: Color,
    val displayTextColor: Color,
    val displayOutlineThickness: Dp
)
