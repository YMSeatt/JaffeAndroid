package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.myapplication.data.Student

import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_BG_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_HEIGHT_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_PADDING_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_WIDTH_DP

import com.example.myapplication.utils.safeParseColor

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
    defaultOutlineThickness: Int = DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP,
    defaultCornerRadius: Int = DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP,
    defaultPadding: Int = DEFAULT_STUDENT_BOX_PADDING_DP,
    defaultFontFamily: String,
    defaultFontSize: Int,
    defaultFontColor: String
): StudentUiItem {
    val backgroundColor = safeParseColor(customBackgroundColor) ?: safeParseColor(defaultBackgroundColor) ?: Color.White
    val outlineColor = safeParseColor(customOutlineColor) ?: safeParseColor(groupColor) ?: safeParseColor(defaultOutlineColor) ?: Color.Black
    val textColor = safeParseColor(customTextColor) ?: safeParseColor(defaultTextColor) ?: Color.Black
    val fontFamily = customFontFamily ?: defaultFontFamily
    val fontSize = customFontSize ?: defaultFontSize
    val fontColor = safeParseColor(customFontColor) ?: safeParseColor(defaultFontColor) ?: Color.Black

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
        displayCornerRadius = (customCornerRadius ?: defaultCornerRadius).dp,
        displayPadding = (customPadding ?: defaultPadding).dp,
        recentBehaviorDescription = recentBehaviorDescription,
        recentHomeworkDescription = recentHomeworkDescription,
        sessionLogText = sessionLogText,
        groupColor = safeParseColor(groupColor),
        groupId = groupId,
        fontFamily = fontFamily,
        fontSize = fontSize,
        fontColor = fontColor
    )
}
