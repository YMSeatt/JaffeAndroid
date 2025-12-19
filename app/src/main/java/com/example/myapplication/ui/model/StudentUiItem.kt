package com.example.myapplication.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class StudentUiItem(
    val id: Int,
    val fullName: String,
    val nickname: String?,
    val initials: String,
    val xPosition: MutableState<Float>,
    val yPosition: MutableState<Float>,
    val displayWidth: MutableState<Dp>,
    val displayHeight: MutableState<Dp>,
    val displayBackgroundColor: MutableState<List<Color>>,
    val displayOutlineColor: MutableState<List<Color>>,
    val displayTextColor: MutableState<Color>,
    val displayOutlineThickness: MutableState<Dp>,
    val displayCornerRadius: MutableState<Dp>,
    val displayPadding: MutableState<Dp>,
    val fontFamily: MutableState<String>,
    val fontSize: MutableState<Int>,
    val fontColor: MutableState<Color>,
    val recentBehaviorDescription: List<String>,
    val recentHomeworkDescription: List<String>,
    val recentQuizDescription: List<String>,
    val groupColor: Color?,
    val groupId: Long?,
    val sessionLogText: List<String> = emptyList(),
    val temporaryTask: String?
)
