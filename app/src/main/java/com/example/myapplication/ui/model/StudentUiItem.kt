package com.example.myapplication.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class StudentUiItem(
    val id: Int,
    val fullName: MutableState<String>,
    val nickname: MutableState<String?>,
    val initials: MutableState<String>,
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
    val recentBehaviorDescription: MutableState<List<String>>,
    val recentHomeworkDescription: MutableState<List<String>>,
    val recentQuizDescription: MutableState<List<String>>,
    val groupColor: MutableState<Color?>,
    val groupId: MutableState<Long?>,
    val sessionLogText: MutableState<List<String>>,
    val temporaryTask: MutableState<String?>
)
