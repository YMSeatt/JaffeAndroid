package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class StudentUiItem(
    val id: Int,
    val fullName: String,
    val nickname: String?,
    val initials: String,
    val xPosition: Double,
    val yPosition: Double,
    val displayWidth: Dp,
    val displayHeight: Dp=100.dp,
    val displayBackgroundColor: Color,
    val displayOutlineColor: Color,
    val displayTextColor: Color,
    val displayOutlineThickness: Dp,
    val fontFamily: String,
    val fontSize: Int,
    val fontColor: Color,
    val recentBehaviorDescription: List<String>,
    val recentHomeworkDescription: List<String>,
    val groupColor: Color?,
    val groupId: Long?,
    val sessionLogText: List<String> = emptyList()
)
