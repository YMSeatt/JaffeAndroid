package com.example.myapplication.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A UI-optimized representation of a Student.
 *
 * Unlike the Room entity [com.example.myapplication.data.Student], this class uses [MutableState]
 * for its properties. This architectural choice enables:
 * 1. **Fine-grained Recomposition**: Compose can observe and react to changes in individual fields
 *    (e.g., just the position or just a color) without recomposing the entire seating chart or
 *    even the entire student icon if not necessary.
 * 2. **Instance Reuse**: The [com.example.myapplication.viewmodel.SeatingChartViewModel] maintains
 *    a cache of these items, updating their internal state via [updateStudentUiItem] rather than
 *    recreating objects. This significantly reduces memory pressure and GC overhead during
 *    frequent updates (like during a drag operation or live quiz session).
 *
 * @property id The unique primary key from the database.
 * @property fullName The combined first and last name for display.
 * @property nickname Optional student nickname.
 * @property initials The 1-3 character abbreviation for the student icon.
 * @property xPosition Logical X coordinate on the 4000x4000 seating chart canvas.
 * @property yPosition Logical Y coordinate on the 4000x4000 seating chart canvas.
 * @property displayWidth The rendered width of the student icon in DP.
 * @property displayHeight The rendered height of the student icon in DP.
 * @property displayBackgroundColor A list of colors for the icon background (supports layered styles).
 * @property displayOutlineColor A list of colors for the icon border (supports dashed/multi-color borders).
 * @property displayTextColor The primary color for the student's name text.
 * @property displayOutlineThickness The thickness of the icon's border in DP.
 * @property displayCornerRadius The corner rounding for the icon card in DP.
 * @property displayPadding Internal padding between the icon border and content.
 * @property fontFamily The typeface name used for student labels.
 * @property fontSize The text size in SP.
 * @property fontColor The color of additional log text.
 * @property recentBehaviorDescription Formatted strings representing the last few behavior events.
 * @property recentHomeworkDescription Formatted strings representing the last few homework logs.
 * @property recentQuizDescription Formatted strings representing the last few quiz logs.
 * @property groupColor The color inherited from the student's group, used for baseline styling.
 * @property groupId The database ID of the student's assigned group.
 * @property sessionLogText Cumulative feedback specific to the current active session.
 * @property temporaryTask A short-term classroom instruction assigned to the student.
 * @property irisParams Procedural parameters for the "Ghost Iris" fractal signature.
 * @property osmoticNode Data representing the student's state in the "Ghost Osmosis" simulation.
 * @property altitude The "Zenith" elevation used for 3D parallax and shadow effects.
 * @property behaviorEntropy A metric of behavioral turbulence calculated by the "Ghost Entropy" engine.
 * @property tectonicStress A metric of social friction calculated by the "Ghost Tectonics" engine.
 */
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
    val temporaryTask: MutableState<String?>,
    val irisParams: MutableState<com.example.myapplication.labs.ghost.GhostIrisEngine.IrisParameters?>,
    val osmoticNode: MutableState<com.example.myapplication.labs.ghost.osmosis.GhostOsmosisEngine.OsmoticNode?>,
    val altitude: MutableState<Float>,
    val behaviorEntropy: MutableState<Float>,
    val tectonicStress: MutableState<Float>
)
