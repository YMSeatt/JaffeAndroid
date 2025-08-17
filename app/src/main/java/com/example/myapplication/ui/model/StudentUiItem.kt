package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class StudentUiItem(
    val id: Int,
    val fullName: String,
    val initials: String,
    val xPosition: Double,
    val yPosition: Double,
    val displayWidth: Dp,
    val displayHeight: Dp,
    val displayBackgroundColor: Color,
    val displayOutlineColor: Color,
    val displayTextColor: Color,
    val displayOutlineThickness: Dp, // Assuming this is needed, similar to StudentDetailsForDisplay
    val recentBehaviorDescription: String?,
    val groupColor: Color?
)
