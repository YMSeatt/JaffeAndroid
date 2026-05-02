@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
package com.example.myapplication.util

import android.util.Log
import android.util.LruCache
import java.util.concurrent.ConcurrentHashMap
import com.example.myapplication.data.Student
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.HomeworkLog
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi

/**
 * Represents a time range and set of days when a conditional formatting rule is active.
 *
 * ### Architectural Intent:
 * These objects are evaluated during the **Stage 2: Transformation** phase of the seating chart update
 * pipeline (see [com.example.myapplication.viewmodel.SeatingChartViewModel.updateStudentsForDisplay]).
 *
 * ### Performance Strategy:
 * Using pre-formatted "HH:mm" strings for time comparison allows the engine to perform temporal
 * filtering using simple string range checks (`currentTimeString in start..end`). This avoids the
 * significant object churn associated with repeatedly creating `Calendar`, `Date`, or `ZonedDateTime`
 * objects for every student icon during high-frequency UI updates.
 *
 * @property startTime The start time in "HH:mm" format (24-hour, e.g., "08:30").
 * @property endTime The end time in "HH:mm" format (24-hour, e.g., "15:45").
 * @property daysOfWeek List of integers representing days of the week (0=Monday, 6=Sunday).
 */
@Serializable
data class ActiveTime(
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("days_of_week") val daysOfWeek: List<Int>
)

/**
 * Defines the criteria that must be met for a conditional formatting rule to be applied.
 * This class is serialized to JSON and stored in the [ConditionalFormattingRule.conditionJson] column.
 *
 * ### Condition Type Schema:
 * Each `type` utilizes a specific subset of properties. Unused properties should be null in the JSON.
 *
 * - **`group`**:
 *   - [groupId]: The database `Long` ID of the student group.
 *   - *Example*: `{"type": "group", "group_id": 5}`
 * - **`behavior_count`**:
 *   - [behaviorNames]: Comma-separated list of behavior types (e.g., "Participating, Helping").
 *   - [countThreshold]: The integer threshold (e.g., 3).
 *   - [timeWindowHours]: Hours to look back (e.g., 48).
 *   - *Example*: `{"type": "behavior_count", "behavior_names": "Negative", "count_threshold": 2, "time_window_hours": 24}`
 * - **`quiz_score_threshold`**:
 *   - [quizNameContains]: Substring to match quiz names.
 *   - [scoreThresholdPercent]: Percentage value (0.0 to 100.0).
 *   - [operator]: One of `<=`, `>=`, `==`, `<`, `>`.
 *   - *Example*: `{"type": "quiz_score_threshold", "quiz_name_contains": "Midterm", "score_threshold_percent": 60.0, "operator": "<"}`
 * - **`live_quiz_response`**:
 *   - [quizResponse]: String to match the latest response in a session.
 *   - *Example*: `{"type": "live_quiz_response", "quiz_response": "Correct"}`
 * - **`live_homework_yes_no`**:
 *   - [homeworkTypeId]: The key for the specific homework step.
 *   - [homeworkResponse]: The status string (e.g., "Done").
 *   - *Example*: `{"type": "live_homework_yes_no", "homework_type_id": "Reading", "homework_response": "Done"}`
 * - **`live_homework_select`**:
 *   - [homeworkOptionName]: The specific option in a multi-select step.
 *   - *Example*: `{"type": "live_homework_select", "homework_option_name": "Signed"}`
 * - **`quiz_mark_count`**:
 *   - [markTypeId]: The ID/Name of the mark (e.g., "Half Credit").
 *   - [markCountThreshold]: The integer threshold.
 *   - [markOperator]: One of `<=`, `>=`, `==`, `<`, `>`, `!=`.
 *   - *Example*: `{"type": "quiz_mark_count", "mark_type_id": "Correct", "mark_count_threshold": 5, "mark_operator": ">="}`
 *
 * ### Global Filters:
 * These filters are evaluated before the specific condition type logic to allow for early-exit optimization.
 * - [activeModes]: List of UI modes (`behavior`, `quiz`, `homework`) where the rule is active.
 * - [activeTimes]: Specific [ActiveTime] windows and days when the rule is evaluated.
 *
 * @property type The category of the condition.
 * @property groupId Used by `group` type.
 * @property behaviorNames Used by `behavior_count` type.
 * @property countThreshold Used by `behavior_count` type.
 * @property timeWindowHours Used by `behavior_count` type.
 * @property quizResponse Used by `live_quiz_response` type.
 * @property homeworkTypeId Used by live homework types.
 * @property homeworkResponse Used by `live_homework_yes_no` type.
 * @property homeworkOptionName Used by `live_homework_select` type.
 * @property operator Used by `quiz_score_threshold` type.
 * @property quizNameContains Used by `quiz_score_threshold` type.
 * @property scoreThresholdPercent Used by `quiz_score_threshold` type.
 * @property markTypeId Used by `quiz_mark_count` type.
 * @property markOperator Used by `quiz_mark_count` type.
 * @property markCountThreshold Used by `quiz_mark_count` type.
 * @property activeModes Optional mode-based activation filter.
 * @property activeTimes Optional temporal activation filter.
 */
@Serializable
data class Condition(
    val type: String,
    @SerialName("group_id") val groupId: Long? = null,
    @SerialName("behavior_names") val behaviorNames: String? = null,
    @SerialName("count_threshold") val countThreshold: Int? = null,
    @SerialName("time_window_hours") val timeWindowHours: Int? = null,
    @SerialName("quiz_response") val quizResponse: String? = null,
    @SerialName("homework_type_id") val homeworkTypeId: String? = null,
    @SerialName("homework_response") val homeworkResponse: String? = null,
    @SerialName("homework_option_name") val homeworkOptionName: String? = null,
    val operator: String? = null,
    @SerialName("quiz_name_contains") val quizNameContains: String? = null,
    @SerialName("score_threshold_percent") val scoreThresholdPercent: Double? = null,
    @SerialName("mark_type_id") val markTypeId: String? = null,
    @SerialName("mark_operator") val markOperator: String? = null,
    @SerialName("mark_count_threshold") val markCountThreshold: Int? = null,
    @SerialName("active_modes") val activeModes: List<String>? = null,
    @SerialName("active_times") val activeTimes: List<ActiveTime>? = null
)

/**
 * Defines the visual styles to apply when a [Condition] is met.
 *
 * Multiple matching rules can result in stacked formats (e.g., a background color
 * and an outline color applied simultaneously).
 *
 * @property color The background or fill color in Hex format (e.g., "#FF0000").
 * @property outline The border or outline color in Hex format.
 */
@Serializable
data class Format(
    val color: String? = null,
    val outline: String? = null
)

/**
 * A processed version of [ConditionalFormattingRule] where JSON strings have been
 * deserialized into domain objects.
 *
 * ### Performance Context:
 * This class is a key component of the application's **BOLT (Performance-Obsessed)** architecture.
 * By pre-decoding rules into this format *before* the per-student rendering loop starts, the
 * engine avoids:
 * 1. **O(R * S)** JSON deserialization calls (where R = rules, S = students).
 * 2. **O(R * S * B)** string splitting operations for behavior name lists (where B = behavior types).
 *
 * The pre-calculated [behaviorNamesSet] enables O(1) lookups during the behavioral analysis phase
 * of rule evaluation.
 *
 * @property id The unique ID of the rule from the database.
 * @property priority The execution priority (lower numbers are processed first).
 * @property condition The deserialized condition criteria.
 * @property format The deserialized visual format.
 * @property behaviorNamesSet A pre-split set of lowercase behavior names derived from [Condition.behaviorNames].
 */
data class DecodedConditionalFormattingRule(
    val id: Int,
    val priority: Int,
    val condition: Condition,
    val format: Format,
    val behaviorNamesSet: Set<String> = emptySet()
)

/**
 * Pre-calculated time values used to optimize conditional formatting evaluations
 * during high-frequency UI updates.
 *
 * @property dayOfWeek The day of the week (0=Monday, 6=Sunday).
 * @property currentTimeString The current time in "HH:mm" format.
 */
data class FormattingTimeContext(
    val dayOfWeek: Int,
    val currentTimeString: String
)

/**
 * The core engine responsible for evaluating and applying conditional formatting rules.
 *
 * This engine processes [ConditionalFormattingRule] entities by decoding their JSON-based
 * conditions and formats, then checking those conditions against student data, behavior logs,
 * and academic performance.
 *
 * ### Architectural Intent:
 * The [ConditionalFormattingEngine] is designed for high-frequency execution (triggered during
 * every seating chart update, including student dragging). To maintain 60fps performance, it
 * utilizes several optimization strategies:
 * 1. **Prioritized Execution**: Rules are sorted by `priority` so that the most important
 *    styles are resolved first.
 * 2. **Rule Decoding Cache**: Rules are pre-decoded into [DecodedConditionalFormattingRule]
 *    objects in the ViewModel to avoid O(N) JSON parsing during the rendering loop.
 * 3. **Mark Data Memoization**: Uses [decodedMarksCache] (an [LruCache]) to store results of
 *    expensive mark-data JSON parsing from [QuizLog] entities.
 * 4. **Early Exit Filters**: Applies global filters (like [activeModes] and [activeTimes])
 *    before executing complex condition-specific logic.
 */
object ConditionalFormattingEngine {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Cache for decoded marks data JSON strings to avoid redundant deserialization.
     *
     * **Why**: [QuizLog.marksData] contains a JSON map of mark counts. Parsing this JSON
     * for every rule evaluation for every student would create massive GC pressure.
     * This cache ensures that identical JSON strings are parsed only once.
     */
    private val decodedMarksCache = LruCache<String, Map<String, Int>>(1000)

    /**
     * BOLT: Cache for lowercased behavior types.
     *
     * **Why**: Behavior comparisons are case-insensitive. Calling `.lowercase()` creates
     * a new string allocation every time. In a loop of thousands of logs, this adds up.
     * This cache memoizes the lowercase version of common behavior types.
     */
    private val lowercaseCache = LruCache<String, String>(256)

    private fun boltLowercase(input: String): String {
        return lowercaseCache.get(input) ?: input.lowercase().also { lowercaseCache.put(input, it) }
    }

    /**
     * Deserializes a list of [ConditionalFormattingRule] entities into [DecodedConditionalFormattingRule] objects.
     * Rules that fail to decode are logged and skipped. The resulting list is sorted by priority.
     *
     * @param rules The raw rule entities from the database.
     * @return A list of successfully decoded and prioritized rules.
     */
    fun decodeRules(rules: List<ConditionalFormattingRule>): List<DecodedConditionalFormattingRule> {
        return rules.filter { it.enabled }.mapNotNull { rule ->
            try {
                val condition = json.decodeFromString<Condition>(rule.conditionJson)
                val format = json.decodeFromString<Format>(rule.formatJson)
                val behaviorNamesSet = condition.behaviorNames?.split(',')?.map { it.trim().lowercase() }?.toSet() ?: emptySet()
                DecodedConditionalFormattingRule(rule.id, rule.priority, condition, format, behaviorNamesSet)
            } catch (e: Exception) {
                Log.e("ConditionalFormattingEngine", "Error decoding rule ${rule.id}: ${e.message}")
                null
            }
        }.sortedBy { it.priority }
    }

    /**
     * Main entry point for calculating the visual formatting for a student.
     * This method decodes the provided rules before evaluation.
     *
     * @param student The student UI data being rendered.
     * @param rules The list of raw rules to evaluate.
     * @param behaviorLog Complete list of behavior events for context.
     * @param quizLog Complete list of quiz logs for context.
     * @param homeworkLog Complete list of homework logs for context.
     * @param isLiveQuizActive Whether a live quiz session is currently in progress.
     * @param liveQuizScores Map of student ID to their current session data (e.g., "last_response").
     * @param isLiveHomeworkActive Whether a live homework check is currently in progress.
     * @param liveHomeworkScores Map of student ID to their current session data (e.g., assignment names to status).
     * @param currentMode The current UI mode ("behavior", "quiz", "homework").
     * @return A list of color/outline pairs representing all matching rule formats.
     */
    fun applyConditionalFormatting(
        student: Student,
        rules: List<ConditionalFormattingRule>,
        behaviorLog: List<BehaviorEvent>,
        quizLog: List<QuizLog>,
        homeworkLog: List<HomeworkLog>,
        quizMarkTypes: List<com.example.myapplication.data.QuizMarkType>,
        isLiveQuizActive: Boolean,
        liveQuizScores: Map<Long, Map<String, Any>>,
        isLiveHomeworkActive: Boolean,
        liveHomeworkScores: Map<Long, Map<String, Any>>,
        currentMode: String
    ): List<DecodedConditionalFormattingRule> {
        val decodedRules = decodeRules(rules)
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> 0
            java.util.Calendar.TUESDAY -> 1
            java.util.Calendar.WEDNESDAY -> 2
            java.util.Calendar.THURSDAY -> 3
            java.util.Calendar.FRIDAY -> 4
            java.util.Calendar.SATURDAY -> 5
            java.util.Calendar.SUNDAY -> 6
            else -> -1
        }
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val currentTimeString = "${if (hour < 10) "0$hour" else hour}:${if (minute < 10) "0$minute" else minute}"

        return applyConditionalFormattingDecoded(
            student = student,
            rules = decodedRules,
            behaviorLog = behaviorLog,
            quizLog = quizLog,
            homeworkLog = homeworkLog,
            quizMarkTypes = quizMarkTypes,
            isLiveQuizActive = isLiveQuizActive,
            liveQuizScores = liveQuizScores,
            isLiveHomeworkActive = isLiveHomeworkActive,
            liveHomeworkScores = liveHomeworkScores,
            currentMode = currentMode,
            currentTimeMillis = System.currentTimeMillis(),
            timeContext = FormattingTimeContext(dayOfWeek, currentTimeString)
        )
    }

    /**
     * Evaluates a list of already-decoded rules for a student.
     * Reuses pre-calculated time context to optimize performance during high-frequency rendering.
     *
     * @param student The student UI data being rendered.
     * @param rules The list of pre-decoded rules to evaluate.
     * @param behaviorLog Complete list of behavior events for context.
     * @param quizLog Complete list of quiz logs for context.
     * @param homeworkLog Complete list of homework logs for context.
     * @param isLiveQuizActive Whether a live quiz session is currently in progress.
     * @param liveQuizScores Map of student ID to their current session data.
     * @param isLiveHomeworkActive Whether a live homework check is currently in progress.
     * @param liveHomeworkScores Map of student ID to their current session data.
     * @param currentMode The current UI mode.
     * @param currentTimeMillis The current system time (cached).
     * @param timeContext Pre-calculated day and time strings.
     * @return A list of color/outline pairs representing all matching rule formats.
     */
    fun applyConditionalFormattingDecoded(
        student: Student,
        rules: List<DecodedConditionalFormattingRule>,
        behaviorLog: List<BehaviorEvent>,
        quizLog: List<QuizLog>,
        homeworkLog: List<HomeworkLog>,
        quizMarkTypes: List<com.example.myapplication.data.QuizMarkType>,
        isLiveQuizActive: Boolean,
        liveQuizScores: Map<Long, Map<String, Any>>,
        isLiveHomeworkActive: Boolean,
        liveHomeworkScores: Map<Long, Map<String, Any>>,
        currentMode: String,
        currentTimeMillis: Long,
        timeContext: FormattingTimeContext
    ): List<DecodedConditionalFormattingRule> {
        var matchingRules: MutableList<DecodedConditionalFormattingRule>? = null

        for (rule in rules) {
            if (checkCondition(
                    student,
                    rule,
                    behaviorLog,
                    quizLog,
                    homeworkLog,
                    quizMarkTypes,
                    isLiveQuizActive,
                    liveQuizScores,
                    isLiveHomeworkActive,
                    liveHomeworkScores,
                    currentMode,
                    currentTimeMillis,
                    timeContext
                )
            ) {
                if (matchingRules == null) {
                    matchingRules = mutableListOf()
                }
                matchingRules.add(rule)
            }
        }

        return matchingRules ?: java.util.Collections.emptyList()
    }

    /**
     * Determines if a specific rule's condition is met for the given student and context.
     *
     * This evaluation happens inside the Stage 2 transformation of the seating chart update
     * pipeline. It is designed to be highly efficient, leveraging BOLT optimizations to
     * avoid redundant computation.
     *
     * ### Evaluation Logic:
     * 1. **Global Mode Filter**: Rules with [Condition.activeModes] only execute if the
     *    current UI mode matches one of the specified modes.
     * 2. **Global Time Filter**: Rules with [Condition.activeTimes] only execute if the
     *    current system time falls within the specified ranges.
     * 3. **Behavioral Analysis**: Rules using `behavior_count` leverage manual loops and early
     *    returns to quickly count incidents within the sliding time window.
     * 4. **Academic Analysis**: Rules using `quiz_score_threshold` or `quiz_mark_count`
     *    evaluate historical data and use [decodedMarksCache] to minimize JSON overhead.
     * 5. **Live Session Analysis**: Rules using `live_quiz_*` or `live_homework_*` check
     *    real-time session state from the [liveQuizScores] and [liveHomeworkScores] maps.
     *
     * @param student The student UI data being rendered.
     * @param rule The pre-decoded rule to evaluate.
     * @param behaviorLog Complete list of behavior events for context.
     * @param quizLog Complete list of quiz logs for context.
     * @param homeworkLog Complete list of homework logs for context.
     * @param isLiveQuizActive Whether a live quiz session is currently in progress.
     * @param liveQuizScores Map of student ID to their current session data.
     * @param isLiveHomeworkActive Whether a live homework check is currently in progress.
     * @param liveHomeworkScores Map of student ID to their current session data.
     * @param currentMode The current UI mode.
     * @param currentTimeMillis The current system time (cached).
     * @param timeContext Pre-calculated day and time strings.
     * @return True if the condition matches, false otherwise.
     */
    private fun checkCondition(
        student: Student,
        rule: DecodedConditionalFormattingRule,
        behaviorLog: List<BehaviorEvent>,
        quizLog: List<QuizLog>,
        homeworkLog: List<HomeworkLog>,
        quizMarkTypes: List<com.example.myapplication.data.QuizMarkType>,
        isLiveQuizActive: Boolean,
        liveQuizScores: Map<Long, Map<String, Any>>,
        isLiveHomeworkActive: Boolean,
        liveHomeworkScores: Map<Long, Map<String, Any>>,
        currentMode: String,
        currentTimeMillis: Long,
        timeContext: FormattingTimeContext
    ): Boolean {
        val condition = rule.condition

        // 1. Global Filter: Application Mode
        // Ensures the rule only triggers when the app is in specific states (e.g., only in "quiz" mode).
        condition.activeModes?.let { activeModes ->
            if (activeModes.isNotEmpty() && currentMode !in activeModes) {
                return false
            }
        }

        // 2. Global Filter: Active Time Ranges
        // Allows rules to be restricted to specific times of day or days of the week.
        condition.activeTimes?.let { activeTimes ->
            if (activeTimes.isNotEmpty()) {
                val dayOfWeek = timeContext.dayOfWeek
                val currentTimeString = timeContext.currentTimeString

                val isTimeValid = activeTimes.any { activeTime ->
                    val start = activeTime.startTime
                    val end = activeTime.endTime
                    val days = activeTime.daysOfWeek

                    currentTimeString in start..end && dayOfWeek in days
                }

                if (!isTimeValid) {
                    return false
                }
            }
        }

        // 3. Specific Condition Type Evaluation
        return when (condition.type) {
            // Checks if the student belongs to a specific group.
            "group" -> student.groupId == condition.groupId

            // Counts specific behavior events within a sliding time window.
            "behavior_count" -> {
                val behaviorNames = rule.behaviorNamesSet
                val countThreshold = condition.countThreshold ?: return false
                val timeWindowHours = condition.timeWindowHours ?: 24
                val cutoffTime = currentTimeMillis - timeWindowHours.toLong() * 3600000L

                // BOLT: Manual count with early break since behaviorLog is sorted DESC
                var count = 0
                for (event in behaviorLog) {
                    if (event.timestamp < cutoffTime) break
                    if (behaviorNames.contains(boltLowercase(event.type))) {
                        count++
                        if (count >= countThreshold) return true // BOLT: Return early if threshold met
                    }
                }
                count >= countThreshold
            }

            // Evaluates historical quiz performance against a percentage threshold.
            "quiz_score_threshold" -> {
                val operator = condition.operator ?: return false
                val quizNameContains = condition.quizNameContains ?: ""
                val scoreThresholdPercent = condition.scoreThresholdPercent ?: return false

                quizLog.any { log ->
                    // BOLT: Removed redundant studentId check as quizLog is already student-specific
                    if (quizNameContains.isNotEmpty() && !log.quizName.contains(quizNameContains, ignoreCase = true)) return@any false

                    val percentage = QuizScoreEngine.calculatePercentage(log, quizMarkTypes)
                    if (percentage != null) {
                        when (operator) {
                            "<=" -> percentage <= scoreThresholdPercent
                            ">=" -> percentage >= scoreThresholdPercent
                            "==" -> percentage == scoreThresholdPercent
                            "<" -> percentage < scoreThresholdPercent
                            ">" -> percentage > scoreThresholdPercent
                            else -> false
                        }
                    } else {
                        false
                    }
                }
            }

            // Checks the current student response in an active live quiz session.
            "live_quiz_response" -> {
                if (!isLiveQuizActive) return false
                val studentScores = liveQuizScores[student.id.toLong()] ?: return false
                val lastResponse = studentScores["last_response"] as? String ?: return false
                lastResponse.equals(condition.quizResponse, ignoreCase = true)
            }

            // Checks the Yes/No status of a specific task in an active live homework session.
            "live_homework_yes_no" -> {
                if (!isLiveHomeworkActive) return false
                val studentScores = liveHomeworkScores[student.id.toLong()] ?: return false
                val homeworkTypeId = condition.homeworkTypeId ?: return false
                val homeworkResponse = condition.homeworkResponse ?: return false
                val studentResponse = studentScores[homeworkTypeId] as? String ?: return false
                studentResponse.equals(homeworkResponse, ignoreCase = true)
            }

            // Checks if a specific option is selected in an active live homework session.
            "live_homework_select" -> {
                if (!isLiveHomeworkActive) return false
                val studentScores = liveHomeworkScores[student.id.toLong()] ?: return false
                val homeworkOptionName = condition.homeworkOptionName ?: return false
                val selectedOptions = studentScores["selected_options"] as? List<*> ?: return false
                selectedOptions.any { it.toString().equals(homeworkOptionName, ignoreCase = true) }
            }

            // Counts occurrences of a specific mark type (e.g., "Correct", "Half Credit") in quiz logs.
            "quiz_mark_count" -> {
                val markTypeId = condition.markTypeId ?: return false
                val operator = condition.markOperator ?: return false
                val countThreshold = condition.markCountThreshold ?: return false

                quizLog.any { log ->
                    // BOLT: Removed redundant studentId check
                    try {
                        val marksData = QuizScoreEngine.getMarksData(log)
                        val count = marksData[markTypeId] ?: 0
                        when (operator) {
                            ">=" -> count >= countThreshold
                            "<=" -> count <= countThreshold
                            "==" -> count == countThreshold
                            ">" -> count > countThreshold
                            "<" -> count < countThreshold
                            "!=" -> count != countThreshold
                            else -> false
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            else -> false
        }
    }
}
