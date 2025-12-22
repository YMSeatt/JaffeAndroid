package com.example.myapplication.ui.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Student
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_BG_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_HEIGHT_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_PADDING_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_WIDTH_DP
import com.example.myapplication.util.safeParseColor

fun Student.toStudentUiItem(
    recentBehaviorDescription: List<String>,
    recentHomeworkDescription: List<String>,
    recentQuizDescription: List<String>,
    sessionLogText: List<String>,
    groupColor: String?,
    conditionalFormattingResult: List<Pair<String?, String?>>,
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
    val customOutlineColor = customOutlineColor?.let { safeParseColor(it) }
    val customTextColor = customTextColor?.let { safeParseColor(it) }

    val baseBackgroundColor = (customBackgroundColor?.let { safeParseColor(it) } ?: safeParseColor(defaultBackgroundColor)).copy(alpha = 1f)
    val baseOutlineColor = customOutlineColor ?: groupColor?.let { safeParseColor(it) } ?: safeParseColor(defaultOutlineColor) ?: Color.Black

    val formattedColors = if (conditionalFormattingResult.isNotEmpty()) {
        conditionalFormattingResult.mapNotNull {
            it.first?.let { colorStr -> safeParseColor(colorStr) }
                ?.takeIf { color -> color.alpha > 0f }
                ?.copy(alpha = 1f)
        }
    } else {
        emptyList()
    }

    val backgroundColors = if (formattedColors.isNotEmpty()) {
        formattedColors
    } else {
        listOf(baseBackgroundColor)
    }

    val outlineColors = if (conditionalFormattingResult.isNotEmpty()) {
        conditionalFormattingResult.map { it.second?.let { colorString -> safeParseColor(colorString) }
            ?: baseOutlineColor }
    } else {
        listOf(baseOutlineColor)
    }

    val textColor = customTextColor ?: safeParseColor(defaultTextColor) ?: Color.Black
    val fontFamily = customFontFamily ?: defaultFontFamily
    val fontSize = customFontSize ?: defaultFontSize
    val fontColor = customFontColor?.let { safeParseColor(it) } ?: safeParseColor(defaultFontColor) ?: Color.Black

    return StudentUiItem(
        id = this.id.toInt(),
        fullName = "$firstName $lastName",
        nickname = nickname,
        initials = "${firstName.first()}${lastName.first()}",
        xPosition = mutableStateOf(xPosition.toFloat()),
        yPosition = mutableStateOf(yPosition.toFloat()),
        displayWidth = mutableStateOf((customWidth ?: defaultWidth).dp),
        displayHeight = mutableStateOf((customHeight ?: defaultHeight).dp),
        displayBackgroundColor = mutableStateOf(backgroundColors),
        displayOutlineColor = mutableStateOf(outlineColors),
        displayTextColor = mutableStateOf(textColor),
        displayOutlineThickness = mutableStateOf((customOutlineThickness ?: defaultOutlineThickness).dp),
        displayCornerRadius = mutableStateOf((customCornerRadius ?: defaultCornerRadius).dp),
        displayPadding = mutableStateOf((customPadding ?: defaultPadding).dp),
        recentBehaviorDescription = recentBehaviorDescription,
        recentHomeworkDescription = recentHomeworkDescription,
        recentQuizDescription = recentQuizDescription,
        sessionLogText = sessionLogText,
        groupColor = groupColor?.let { safeParseColor(it) },
        groupId = groupId,
        fontFamily = mutableStateOf(fontFamily),
        fontSize = mutableStateOf(fontSize),
        fontColor = mutableStateOf(fontColor),
        temporaryTask = temporaryTask
    )
}
