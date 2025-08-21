package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.myapplication.data.Student

import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_BG_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_HEIGHT_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_WIDTH_DP

fun Student.toStudentUiItem(
    recentBehaviorDescription: List<String>,
    recentHomeworkDescription: List<String>,
    sessionLogText: List<String>,
    groupColor: String?,
    defaultWidth: Int = DEFAULT_STUDENT_BOX_WIDTH_DP,
    defaultHeight: Int = DEFAULT_STUDENT_BOX_HEIGHT_DP,
    defaultBackgroundColor: String = DEFAULT_STUDENT_BOX_BG_COLOR_HEX,
    defaultOutlineColor: String = DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX,
    defaultTextColor: String = DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX,
    defaultOutlineThickness: Int = DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP
): StudentUiItem {
    val backgroundColor = customBackgroundColor?.let { Color(it.toColorInt()) } ?: Color(defaultBackgroundColor.toColorInt())
    val outlineColor = customOutlineColor?.let { Color(it.toColorInt()) } ?: groupColor?.let { Color(it.toColorInt()) } ?: Color(defaultOutlineColor.toColorInt())
    val textColor = customTextColor?.let { Color(it.toColorInt()) } ?: Color(defaultTextColor.toColorInt())

    return StudentUiItem(
        id = this.id.toInt(),
        fullName = "$firstName $lastName",
        nickname = nickname,
        initials = "${firstName.first()}${lastName.first()}",
        xPosition = xPosition.toDouble(),
        yPosition = yPosition.toDouble(),
        displayWidth = (customWidth ?: defaultWidth).dp,
        displayHeight = (customHeight ?: defaultHeight).dp,
        displayBackgroundColor = backgroundColor,
        displayOutlineColor = outlineColor,
        displayTextColor = textColor,
        displayOutlineThickness = (customOutlineThickness ?: defaultOutlineThickness).dp,
        recentBehaviorDescription = recentBehaviorDescription,
        recentHomeworkDescription = recentHomeworkDescription,
        sessionLogText = sessionLogText,
        groupColor = groupColor?.let { Color(it.toColorInt()) },
        groupId = groupId
    )
}
