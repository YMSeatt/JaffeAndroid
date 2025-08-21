package com.example.myapplication.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times

fun calculateStudentIconHeight(
    defaultHeight: Dp,
    showBehavior: Boolean,
    behaviorText: String,
    homeworkText: String,
    sessionLogText: String,
    lineHeight: Dp
): Dp {
    var totalHeight = defaultHeight
    if (showBehavior) {
        val behaviorLines = behaviorText.lines().size
        val homeworkLines = homeworkText.lines().size
        val sessionLogLines = sessionLogText.lines().size
        totalHeight += (behaviorLines + homeworkLines + sessionLogLines) * lineHeight
    }
    return totalHeight
}
