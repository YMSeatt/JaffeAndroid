package com.example.myapplication.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times

/**
 * Utility for calculating the dynamic height of a student icon based on its content.
 *
 * ### ⚠️ Legacy Component:
 * This function belongs to the application's legacy layout model. In the modern
 * "Fluid Interaction" model, student icons utilize `Modifier.heightIn` or the
 * `autoExpandStudentBoxes` preference to manage their size. This calculator is
 * maintained for backward compatibility with older seating chart snapshots and
 * manual layout logic.
 *
 * @param defaultHeight The base height of the icon (containing the student name).
 * @param showBehavior Whether behavioral logs are currently being displayed.
 * @param behaviorText The raw string of behavior logs.
 * @param homeworkText The raw string of homework logs.
 * @param sessionLogText The raw string of session logs.
 * @param lineHeight The height of a single line of text in DP.
 * @return The total calculated height required to fit all content.
 */
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
