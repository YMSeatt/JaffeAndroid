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
import com.example.myapplication.util.DecodedConditionalFormattingRule
import com.example.myapplication.util.safeParseColor

/**
 * Creates a new [StudentUiItem] from a [Student] entity and associated context data.
 */
fun Student.toStudentUiItem(
    fullName: String,
    recentBehaviorDescription: List<String>,
    recentHomeworkDescription: List<String>,
    recentQuizDescription: List<String>,
    sessionLogText: List<String>,
    groupColor: Color?,
    backgroundColors: List<Color>,
    outlineColors: List<Color>,
    textColor: Color,
    fontColor: Color,
    defaultWidth: Int = DEFAULT_STUDENT_BOX_WIDTH_DP,
    defaultHeight: Int = DEFAULT_STUDENT_BOX_HEIGHT_DP,
    defaultOutlineThickness: Int = DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP,
    defaultCornerRadius: Int = DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP,
    defaultPadding: Int = DEFAULT_STUDENT_BOX_PADDING_DP,
    defaultFontFamily: String,
    defaultFontSize: Int
): StudentUiItem {
    return StudentUiItem(
        id = this.id.toInt(),
        fullName = mutableStateOf(fullName),
        nickname = mutableStateOf(nickname),
        initials = mutableStateOf(getEffectiveInitials()),
        xPosition = mutableStateOf(xPosition),
        yPosition = mutableStateOf(yPosition),
        displayWidth = mutableStateOf((customWidth ?: defaultWidth).dp),
        displayHeight = mutableStateOf((customHeight ?: defaultHeight).dp),
        displayBackgroundColor = mutableStateOf(backgroundColors),
        displayOutlineColor = mutableStateOf(outlineColors),
        displayTextColor = mutableStateOf(textColor),
        displayOutlineThickness = mutableStateOf((customOutlineThickness ?: defaultOutlineThickness).dp),
        displayCornerRadius = mutableStateOf((customCornerRadius ?: defaultCornerRadius).dp),
        displayPadding = mutableStateOf((customPadding ?: defaultPadding).dp),
        recentBehaviorDescription = mutableStateOf(recentBehaviorDescription),
        recentHomeworkDescription = mutableStateOf(recentHomeworkDescription),
        recentQuizDescription = mutableStateOf(recentQuizDescription),
        sessionLogText = mutableStateOf(sessionLogText),
        groupColor = mutableStateOf(groupColor),
        groupId = mutableStateOf(groupId),
        fontFamily = mutableStateOf(customFontFamily ?: defaultFontFamily),
        fontSize = mutableStateOf(customFontSize ?: defaultFontSize),
        fontColor = mutableStateOf(fontColor),
        isPinned = mutableStateOf(this.isPinned)
    )
}

/**
 * Updates an existing [StudentUiItem] with fresh data from a [Student] entity.
 */
fun Student.updateStudentUiItem(
    item: StudentUiItem,
    fullName: String,
    recentBehaviorDescription: List<String>,
    recentHomeworkDescription: List<String>,
    recentQuizDescription: List<String>,
    sessionLogText: List<String>,
    groupColor: Color?,
    backgroundColors: List<Color>,
    outlineColors: List<Color>,
    textColor: Color,
    fontColor: Color,
    defaultWidth: Int = DEFAULT_STUDENT_BOX_WIDTH_DP,
    defaultHeight: Int = DEFAULT_STUDENT_BOX_HEIGHT_DP,
    defaultOutlineThickness: Int = DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP,
    defaultCornerRadius: Int = DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP,
    defaultPadding: Int = DEFAULT_STUDENT_BOX_PADDING_DP,
    defaultFontFamily: String,
    defaultFontSize: Int
) {
    updateIfChanged(item.fullName, fullName)
    updateIfChanged(item.nickname, nickname)
    updateIfChanged(item.initials, getEffectiveInitials())
    updateIfChanged(item.xPosition, xPosition)
    updateIfChanged(item.yPosition, yPosition)
    updateIfChanged(item.displayWidth, (customWidth ?: defaultWidth).dp)
    updateIfChanged(item.displayHeight, (customHeight ?: defaultHeight).dp)
    updateIfChanged(item.displayBackgroundColor, backgroundColors)
    updateIfChanged(item.displayOutlineColor, outlineColors)
    updateIfChanged(item.displayTextColor, textColor)
    updateIfChanged(item.displayOutlineThickness, (customOutlineThickness ?: defaultOutlineThickness).dp)
    updateIfChanged(item.displayCornerRadius, (customCornerRadius ?: defaultCornerRadius).dp)
    updateIfChanged(item.displayPadding, (customPadding ?: defaultPadding).dp)
    updateIfChanged(item.recentBehaviorDescription, recentBehaviorDescription)
    updateIfChanged(item.recentHomeworkDescription, recentHomeworkDescription)
    updateIfChanged(item.recentQuizDescription, recentQuizDescription)
    updateIfChanged(item.sessionLogText, sessionLogText)
    updateIfChanged(item.groupColor, groupColor)
    updateIfChanged(item.groupId, groupId)
    updateIfChanged(item.fontFamily, customFontFamily ?: defaultFontFamily)
    updateIfChanged(item.fontSize, customFontSize ?: defaultFontSize)
    updateIfChanged(item.fontColor, fontColor)
    updateIfChanged(item.isPinned, this.isPinned)
}

private fun <T> updateIfChanged(state: MutableState<T>, newValue: T) {
    if (state.value != newValue) {
        state.value = newValue
    }
}

data class StudentStyles(
    val backgroundColors: List<Color>,
    val outlineColors: List<Color>,
    val textColor: Color,
    val fontColor: Color
)

private val singletonColorListCache = java.util.concurrent.ConcurrentHashMap<Color, List<Color>>()

private fun getSingletonColorList(color: Color): List<Color> {
    return singletonColorListCache.getOrPut(color) { java.util.Collections.singletonList(color) }
}

fun Student.calculateStyles(
    groupColor: Color?,
    matchingRules: List<DecodedConditionalFormattingRule>,
    liveQuizProgressColor: Color?,
    defaultBackgroundColor: Color,
    defaultOutlineColor: Color,
    defaultTextColor: Color,
    defaultFontColor: Color
): StudentStyles {
    val customOutlineColor = customOutlineColor?.let { safeParseColor(it) }
    val customTextColor = customTextColor?.let { safeParseColor(it) }

    val baseBackgroundColor = (customBackgroundColor?.let { safeParseColor(it) } ?: defaultBackgroundColor).copy(alpha = 1f)
    val baseOutlineColor = liveQuizProgressColor ?: customOutlineColor ?: groupColor ?: defaultOutlineColor

    val overrideRule = matchingRules.find { it.format.applicationStyle == "override" }

    val backgroundColors: List<Color>
    val outlineColors: List<Color>

    if (overrideRule != null) {
        val overrideBackground = overrideRule.format.color?.let { safeParseColor(it) }?.copy(alpha = 1f)
        backgroundColors = getSingletonColorList(overrideBackground ?: baseBackgroundColor)

        val overrideOutline = overrideRule.format.outline?.let { safeParseColor(it) }
        outlineColors = getSingletonColorList(overrideOutline ?: baseOutlineColor)
    } else {
        val formattedColors = if (matchingRules.isNotEmpty()) {
            matchingRules.mapNotNull {
                it.format.color?.let { colorStr -> safeParseColor(colorStr) }
                    ?.takeIf { color -> color.alpha > 0f }
                    ?.copy(alpha = 1f)
            }
        } else {
            emptyList()
        }

        backgroundColors = if (formattedColors.isNotEmpty()) {
            formattedColors
        } else {
            getSingletonColorList(baseBackgroundColor)
        }

        outlineColors = if (matchingRules.isNotEmpty()) {
            matchingRules.map { rule ->
                rule.format.outline?.let { colorString -> safeParseColor(colorString) } ?: baseOutlineColor
            }
        } else {
            getSingletonColorList(baseOutlineColor)
        }
    }

    val textColor = customTextColor ?: defaultTextColor
    val fontColor = customFontColor?.let { safeParseColor(it) } ?: defaultFontColor

    return StudentStyles(backgroundColors, outlineColors, textColor, fontColor)
}
