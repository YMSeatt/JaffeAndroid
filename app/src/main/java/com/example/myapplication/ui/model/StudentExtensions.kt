package com.example.myapplication.ui.model

import androidx.compose.runtime.MutableState
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
    val styles = calculateStyles(
        groupColor,
        conditionalFormattingResult,
        defaultBackgroundColor,
        defaultOutlineColor,
        defaultTextColor,
        defaultFontColor
    )

    return StudentUiItem(
        id = this.id.toInt(),
        fullName = mutableStateOf("$firstName $lastName"),
        nickname = mutableStateOf(nickname),
        initials = mutableStateOf("${firstName.first()}${lastName.first()}"),
        xPosition = mutableStateOf(xPosition),
        yPosition = mutableStateOf(yPosition),
        displayWidth = mutableStateOf((customWidth ?: defaultWidth).dp),
        displayHeight = mutableStateOf((customHeight ?: defaultHeight).dp),
        displayBackgroundColor = mutableStateOf(styles.backgroundColors),
        displayOutlineColor = mutableStateOf(styles.outlineColors),
        displayTextColor = mutableStateOf(styles.textColor),
        displayOutlineThickness = mutableStateOf((customOutlineThickness ?: defaultOutlineThickness).dp),
        displayCornerRadius = mutableStateOf((customCornerRadius ?: defaultCornerRadius).dp),
        displayPadding = mutableStateOf((customPadding ?: defaultPadding).dp),
        recentBehaviorDescription = mutableStateOf(recentBehaviorDescription),
        recentHomeworkDescription = mutableStateOf(recentHomeworkDescription),
        recentQuizDescription = mutableStateOf(recentQuizDescription),
        sessionLogText = mutableStateOf(sessionLogText),
        groupColor = mutableStateOf(groupColor?.let { safeParseColor(it) }),
        groupId = mutableStateOf(groupId),
        fontFamily = mutableStateOf(customFontFamily ?: defaultFontFamily),
        fontSize = mutableStateOf(customFontSize ?: defaultFontSize),
        fontColor = mutableStateOf(styles.fontColor),
        temporaryTask = mutableStateOf(temporaryTask)
    )
}

fun Student.updateStudentUiItem(
    item: StudentUiItem,
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
) {
    val styles = calculateStyles(
        groupColor,
        conditionalFormattingResult,
        defaultBackgroundColor,
        defaultOutlineColor,
        defaultTextColor,
        defaultFontColor
    )

    updateIfChanged(item.fullName, "$firstName $lastName")
    updateIfChanged(item.nickname, nickname)
    updateIfChanged(item.initials, "${firstName.first()}${lastName.first()}")
    updateIfChanged(item.xPosition, xPosition)
    updateIfChanged(item.yPosition, yPosition)
    updateIfChanged(item.displayWidth, (customWidth ?: defaultWidth).dp)
    updateIfChanged(item.displayHeight, (customHeight ?: defaultHeight).dp)
    updateIfChanged(item.displayBackgroundColor, styles.backgroundColors)
    updateIfChanged(item.displayOutlineColor, styles.outlineColors)
    updateIfChanged(item.displayTextColor, styles.textColor)
    updateIfChanged(item.displayOutlineThickness, (customOutlineThickness ?: defaultOutlineThickness).dp)
    updateIfChanged(item.displayCornerRadius, (customCornerRadius ?: defaultCornerRadius).dp)
    updateIfChanged(item.displayPadding, (customPadding ?: defaultPadding).dp)
    updateIfChanged(item.recentBehaviorDescription, recentBehaviorDescription)
    updateIfChanged(item.recentHomeworkDescription, recentHomeworkDescription)
    updateIfChanged(item.recentQuizDescription, recentQuizDescription)
    updateIfChanged(item.sessionLogText, sessionLogText)
    updateIfChanged(item.groupColor, groupColor?.let { safeParseColor(it) })
    updateIfChanged(item.groupId, groupId)
    updateIfChanged(item.fontFamily, customFontFamily ?: defaultFontFamily)
    updateIfChanged(item.fontSize, customFontSize ?: defaultFontSize)
    updateIfChanged(item.fontColor, styles.fontColor)
    updateIfChanged(item.temporaryTask, temporaryTask)
}

private fun <T> updateIfChanged(state: MutableState<T>, newValue: T) {
    if (state.value != newValue) {
        state.value = newValue
    }
}

private data class StudentStyles(
    val backgroundColors: List<Color>,
    val outlineColors: List<Color>,
    val textColor: Color,
    val fontColor: Color
)

private fun Student.calculateStyles(
    groupColor: String?,
    conditionalFormattingResult: List<Pair<String?, String?>>,
    defaultBackgroundColor: String,
    defaultOutlineColor: String,
    defaultTextColor: String,
    defaultFontColor: String
): StudentStyles {
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
    val fontColor = customFontColor?.let { safeParseColor(it) } ?: safeParseColor(defaultFontColor) ?: Color.Black

    return StudentStyles(backgroundColors, outlineColors, textColor, fontColor)
}
