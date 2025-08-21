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
    return StudentUiItem(
        id = this.id.toInt(),
        fullName = "${this.firstName} ${this.lastName}",
        nickname = this.nickname,
        initials = this.initials ?: this.getGeneratedInitials(),
        xPosition = this.xPosition.toDouble(),
        yPosition = this.yPosition.toDouble(),
        displayWidth = (this.customWidth ?: defaultWidth).dp,
        displayHeight = (this.customHeight ?: defaultHeight).dp,
        displayBackgroundColor = groupColor?.let { Color(it.toColorInt()) } ?: this.customBackgroundColor?.let { Color(it.toColorInt()) } ?: Color(defaultBackgroundColor.toColorInt()),
        displayOutlineColor = this.customOutlineColor?.let { Color(it.toColorInt()) } ?: Color(defaultOutlineColor.toColorInt()),
        displayTextColor = this.customTextColor?.let { Color(it.toColorInt()) } ?: Color(defaultTextColor.toColorInt()),
        displayOutlineThickness = (this.customOutlineThickness ?: defaultOutlineThickness).dp,
        recentBehaviorDescription = recentBehaviorDescription,
        recentHomeworkDescription = recentHomeworkDescription,
        sessionLogText = sessionLogText,
        groupColor = groupColor?.let { Color(it.toColorInt()) }
    )
}
