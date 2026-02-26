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

/**
 * Creates a new [StudentUiItem] from a [Student] entity and associated context data.
 *
 * This function initializes all [MutableState] fields of the UI item.
 *
 * @param recentBehaviorDescription Formatted list of recent behavior events.
 * @param recentHomeworkDescription Formatted list of recent homework logs.
 * @param recentQuizDescription Formatted list of recent quiz logs.
 * @param sessionLogText Combined history of events for the current session.
 * @param groupColor The Hex color of the student's group, if any.
 * @param conditionalFormattingResult List of active color/outline pairs from the formatting engine.
 * @param defaultFontFamily Global default font family from user preferences.
 * @param defaultFontSize Global default font size from user preferences.
 * @param defaultFontColor Global default font color from user preferences.
 */
fun Student.toStudentUiItem(
    recentBehaviorDescription: List<String>,
    recentHomeworkDescription: List<String>,
    recentQuizDescription: List<String>,
    sessionLogText: List<String>,
    groupColor: String?,
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
        fullName = mutableStateOf("$firstName $lastName"),
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
        groupColor = mutableStateOf(groupColor?.let { safeParseColor(it) }),
        groupId = mutableStateOf(groupId),
        fontFamily = mutableStateOf(customFontFamily ?: defaultFontFamily),
        fontSize = mutableStateOf(customFontSize ?: defaultFontSize),
        fontColor = mutableStateOf(fontColor),
        temporaryTask = mutableStateOf(temporaryTask)
    )
}

/**
 * Updates an existing [StudentUiItem] with fresh data from a [Student] entity.
 *
 * This method is the "Sync" stage of the performance pipeline. It uses [updateIfChanged]
 * to perform differential updates on the [MutableState] fields.
 *
 * **Performance Note:** By updating the *existing* fields of a cached item instead of
 * creating a new [StudentUiItem] instance, we maintain object identity across the
 * seating chart. This allows Jetpack Compose to use its internal slot table optimizations
 * to only recompose the specific UI nodes that depend on the modified fields.
 */
fun Student.updateStudentUiItem(
    item: StudentUiItem,
    recentBehaviorDescription: List<String>,
    recentHomeworkDescription: List<String>,
    recentQuizDescription: List<String>,
    sessionLogText: List<String>,
    groupColor: String?,
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
    updateIfChanged(item.fullName, "$firstName $lastName")
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
    updateIfChanged(item.groupColor, groupColor?.let { safeParseColor(it) })
    updateIfChanged(item.groupId, groupId)
    updateIfChanged(item.fontFamily, customFontFamily ?: defaultFontFamily)
    updateIfChanged(item.fontSize, customFontSize ?: defaultFontSize)
    updateIfChanged(item.fontColor, fontColor)
    updateIfChanged(item.temporaryTask, temporaryTask)
}

/**
 * Helper to update a [MutableState] only if the new value differs from the current one.
 *
 * This is a critical optimization for the Fluid Interaction model. If the value hasn't
 * changed (e.g., the student's name remains the same), setting the `state.value` would
 * still trigger a potential recomposition cycle in Compose. This check prevents that
 * overhead.
 */
private fun <T> updateIfChanged(state: MutableState<T>, newValue: T) {
    if (state.value != newValue) {
        state.value = newValue
    }
}

/**
 * BOLT: Data class to hold resolved styles, exported for use in SeatingChartViewModel's
 * memoized transformation Stage 2.
 */
data class StudentStyles(
    val backgroundColors: List<Color>,
    val outlineColors: List<Color>,
    val textColor: Color,
    val fontColor: Color
)

/**
 * Resolves the visual styling for a student based on a hierarchy of priorities.
 *
 * The styling precedence is as follows (highest to lowest):
 * 1. **Conditional Formatting**: Dynamic rules (e.g., "Score < 50%") override all other styles.
 *    Multiple matching rules result in a list of colors (often rendered as a gradient or border).
 * 2. **User Overrides**: Manual customizations set by the teacher on an individual student.
 * 3. **Group Settings**: Styles inherited from the student's assigned group (e.g., "Blue Group").
 * 4. **Defaults**: Global application defaults for students.
 *
 * @return A [StudentStyles] object containing the resolved colors.
 */
private val singletonColorListCache = java.util.concurrent.ConcurrentHashMap<Color, List<Color>>()

/**
 * BOLT: Optimized helper to get a single-item list of colors without redundant allocations.
 * Reuses a cached list for the given color to avoid thousands of small O(1) list objects.
 */
private fun getSingletonColorList(color: Color): List<Color> {
    return singletonColorListCache.getOrPut(color) { java.util.Collections.singletonList(color) }
}

/**
 * Resolves the visual styling for a student based on a hierarchy of priorities.
 *
 * The styling precedence is as follows (highest to lowest):
 * 1. **Conditional Formatting**: Dynamic rules (e.g., "Score < 50%") override all other styles.
 *    Multiple matching rules result in a list of colors (often rendered as a gradient or border).
 * 2. **User Overrides**: Manual customizations set by the teacher on an individual student.
 * 3. **Group Settings**: Styles inherited from the student's assigned group (e.g., "Blue Group").
 * 4. **Defaults**: Global application defaults for students.
 *
 * @return A [StudentStyles] object containing the resolved colors.
 */
/**
 * Resolves the visual styling for a student based on a hierarchy of priorities.
 * Moved to public/internal for access in SeatingChartViewModel's optimization pipeline.
 */
fun Student.calculateStyles(
    groupColor: String?,
    conditionalFormattingResult: List<Pair<String?, String?>>,
    liveQuizProgressColor: Color?,
    defaultBackgroundColor: String,
    defaultOutlineColor: String,
    defaultTextColor: String,
    defaultFontColor: String
): StudentStyles {
    val customOutlineColor = customOutlineColor?.let { safeParseColor(it) }
    val customTextColor = customTextColor?.let { safeParseColor(it) }

    val baseBackgroundColor = (customBackgroundColor?.let { safeParseColor(it) } ?: safeParseColor(defaultBackgroundColor)).copy(alpha = 1f)
    val baseOutlineColor = liveQuizProgressColor ?: customOutlineColor ?: groupColor?.let { safeParseColor(it) } ?: safeParseColor(defaultOutlineColor) ?: Color.Black

    val formattedColors = if (conditionalFormattingResult.isNotEmpty()) {
        conditionalFormattingResult.mapNotNull {
            it.first?.let { colorStr -> safeParseColor(colorStr) }
                ?.takeIf { color -> color.alpha > 0f }
                ?.copy(alpha = 1f)
        }
    } else {
        emptyList()
    }

    // BOLT: Use cached singleton lists to avoid object churn during high-frequency updates.
    val backgroundColors = if (formattedColors.isNotEmpty()) {
        formattedColors
    } else {
        getSingletonColorList(baseBackgroundColor)
    }

    val outlineColors = if (conditionalFormattingResult.isNotEmpty()) {
        conditionalFormattingResult.map { it.second?.let { colorString -> safeParseColor(colorString) }
            ?: baseOutlineColor }
    } else {
        getSingletonColorList(baseOutlineColor)
    }

    val textColor = customTextColor ?: safeParseColor(defaultTextColor) ?: Color.Black
    val fontColor = customFontColor?.let { safeParseColor(it) } ?: safeParseColor(defaultFontColor) ?: Color.Black

    return StudentStyles(backgroundColors, outlineColors, textColor, fontColor)
}
