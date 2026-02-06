@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
package com.example.myapplication.util

import android.util.Log
import com.example.myapplication.data.StudentDetailsForDisplay
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
 * @property startTime The start time in "HH:MM" format (24-hour).
 * @property endTime The end time in "HH:MM" format (24-hour).
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
 * This class is serialized to JSON and stored in the [ConditionalFormattingRule] entity.
 *
 * @property type The category of the condition (e.g., "group", "behavior_count", "quiz_score_threshold").
 * @property groupId Used when type is "group". Matches students in a specific group.
 * @property behaviorNames Comma-separated list of behavior types to count. Used when type is "behavior_count".
 * @property countThreshold The minimum number of events required to trigger the rule.
 * @property timeWindowHours The look-back period (in hours) for behavior-based rules. Defaults to 24.
 * @property quizResponse The specific response string to match in live quiz sessions.
 * @property homeworkTypeId The assignment name or ID to match for homework rules.
 * @property homeworkResponse The specific status or response to match for homework rules (e.g., "yes", "no").
 * @property homeworkOptionName The specific option name to match in multi-select homework checks.
 * @property operator Comparison operator for numeric thresholds (e.g., "<=", ">=", "==").
 * @property quizNameContains Partial match filter for quiz names in historical quiz rules.
 * @property scoreThresholdPercent Percentage threshold (0-100) for quiz performance rules.
 * @property markTypeId The specific ID of a quiz mark type to count.
 * @property markOperator Comparison operator for mark counts.
 * @property markCountThreshold Threshold value for the number of specific marks received.
 * @property activeModes List of application modes (e.g., "behavior", "quiz", "homework") where this rule applies.
 * @property activeTimes List of [ActiveTime] ranges where this rule applies.
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
 * Defines the visual styles to apply when a condition is met.
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
 * deserialized into [Condition] and [Format] objects.
 *
 * @property id The unique ID of the rule from the database.
 * @property priority The execution priority (lower numbers are processed first).
 * @property condition The deserialized condition criteria.
 * @property format The deserialized visual format.
 * @property behaviorNamesList A pre-split list of behavior names derived from [Condition.behaviorNames].
 */
data class DecodedConditionalFormattingRule(
    val id: Int,
    val priority: Int,
    val condition: Condition,
    val format: Format,
    val behaviorNamesList: List<String> = emptyList()
)

/**
 * The core engine responsible for evaluating and applying conditional formatting rules.
 *
 * This engine processes [ConditionalFormattingRule] entities by decoding their JSON-based
 * conditions and formats, then checking those conditions against student data, behavior logs,
 * and academic performance.
 */
object ConditionalFormattingEngine {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Deserializes a list of [ConditionalFormattingRule] entities into [DecodedConditionalFormattingRule] objects.
     * Rules that fail to decode are logged and skipped. The resulting list is sorted by priority.
     *
     * @param rules The raw rule entities from the database.
     * @return A list of successfully decoded and prioritized rules.
     */
    fun decodeRules(rules: List<ConditionalFormattingRule>): List<DecodedConditionalFormattingRule> {
        return rules.mapNotNull { rule ->
            try {
                val condition = json.decodeFromString<Condition>(rule.conditionJson)
                val format = json.decodeFromString<Format>(rule.formatJson)
                val behaviorNamesList = condition.behaviorNames?.split(',')?.map { it.trim() } ?: emptyList()
                DecodedConditionalFormattingRule(rule.id, rule.priority, condition, format, behaviorNamesList)
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
        student: StudentDetailsForDisplay,
        rules: List<ConditionalFormattingRule>,
        behaviorLog: List<BehaviorEvent>,
        quizLog: List<QuizLog>,
        homeworkLog: List<HomeworkLog>,
        isLiveQuizActive: Boolean,
        liveQuizScores: Map<Long, Map<String, Any>>,
        isLiveHomeworkActive: Boolean,
        liveHomeworkScores: Map<Long, Map<String, Any>>,
        currentMode: String
    ): List<Pair<String?, String?>> {
        val decodedRules = decodeRules(rules)
        return applyConditionalFormattingDecoded(
            student = student,
            rules = decodedRules,
            behaviorLog = behaviorLog,
            quizLog = quizLog,
            homeworkLog = homeworkLog,
            isLiveQuizActive = isLiveQuizActive,
            liveQuizScores = liveQuizScores,
            isLiveHomeworkActive = isLiveHomeworkActive,
            liveHomeworkScores = liveHomeworkScores,
            currentMode = currentMode,
            currentTimeMillis = System.currentTimeMillis(),
            calendar = java.util.Calendar.getInstance()
        )
    }

    /**
     * Evaluates a list of already-decoded rules for a student.
     * Reuses time/calendar objects to optimize performance during high-frequency rendering.
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
     * @param calendar A [java.util.Calendar] instance (reused).
     * @return A list of color/outline pairs representing all matching rule formats.
     */
    fun applyConditionalFormattingDecoded(
        student: StudentDetailsForDisplay,
        rules: List<DecodedConditionalFormattingRule>,
        behaviorLog: List<BehaviorEvent>,
        quizLog: List<QuizLog>,
        homeworkLog: List<HomeworkLog>,
        isLiveQuizActive: Boolean,
        liveQuizScores: Map<Long, Map<String, Any>>,
        isLiveHomeworkActive: Boolean,
        liveHomeworkScores: Map<Long, Map<String, Any>>,
        currentMode: String,
        currentTimeMillis: Long,
        calendar: java.util.Calendar
    ): List<Pair<String?, String?>> {
        val matchingFormats = mutableListOf<Pair<String?, String?>>()

        for (rule in rules) {
            if (checkCondition(
                    student,
                    rule,
                    behaviorLog,
                    quizLog,
                    homeworkLog,
                    isLiveQuizActive,
                    liveQuizScores,
                    isLiveHomeworkActive,
                    liveHomeworkScores,
                    currentMode,
                    currentTimeMillis,
                    calendar
                )
            ) {
                matchingFormats.add(Pair(rule.format.color, rule.format.outline))
            }
        }

        return matchingFormats
    }

    /**
     * Determines if a specific rule's condition is met for the given student and context.
     *
     * @return True if the condition matches, false otherwise.
     */
    private fun checkCondition(
        student: StudentDetailsForDisplay,
        rule: DecodedConditionalFormattingRule,
        behaviorLog: List<BehaviorEvent>,
        quizLog: List<QuizLog>,
        homeworkLog: List<HomeworkLog>,
        isLiveQuizActive: Boolean,
        liveQuizScores: Map<Long, Map<String, Any>>,
        isLiveHomeworkActive: Boolean,
        liveHomeworkScores: Map<Long, Map<String, Any>>,
        currentMode: String,
        currentTimeMillis: Long,
        calendar: java.util.Calendar
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
                val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // Sunday = 1, Saturday = 7
                val currentTimeString = String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))

                val isTimeValid = activeTimes.any { activeTime ->
                    val start = activeTime.startTime
                    val end = activeTime.endTime
                    val days = activeTime.daysOfWeek

                    val calendarDayOfWeek = when (dayOfWeek) {
                        java.util.Calendar.MONDAY -> 0
                        java.util.Calendar.TUESDAY -> 1
                        java.util.Calendar.WEDNESDAY -> 2
                        java.util.Calendar.THURSDAY -> 3
                        java.util.Calendar.FRIDAY -> 4
                        java.util.Calendar.SATURDAY -> 5
                        java.util.Calendar.SUNDAY -> 6
                        else -> -1 // Should not happen
                    }

                    currentTimeString in start..end && calendarDayOfWeek in days
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
                val behaviorNames = rule.behaviorNamesList
                val countThreshold = condition.countThreshold ?: return false
                val timeWindowHours = condition.timeWindowHours ?: 24
                val cutoffTime = currentTimeMillis - timeWindowHours.toLong() * 60 * 60 * 1000

                val count = behaviorLog.count {
                    it.studentId == student.id.toLong() &&
                    behaviorNames.any { name -> it.type.equals(name, ignoreCase = true) } &&
                    it.timestamp >= cutoffTime
                }
                count >= countThreshold
            }

            // Evaluates historical quiz performance against a percentage threshold.
            "quiz_score_threshold" -> {
                val operator = condition.operator ?: return false
                val quizNameContains = condition.quizNameContains ?: ""
                val scoreThresholdPercent = condition.scoreThresholdPercent ?: return false

                quizLog.any { log ->
                    if (log.studentId != student.id.toLong()) return@any false
                    if (quizNameContains.isNotEmpty() && !log.quizName.contains(quizNameContains, ignoreCase = true)) return@any false

                    val score = log.markValue
                    val maxScore = log.maxMarkValue
                    if (score != null && maxScore != null && maxScore > 0) {
                        val percentage = (score.toDouble() / maxScore.toDouble()) * 100 // Ensure floating-point division
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
                    if (log.studentId != student.id.toLong()) return@any false
                    try {
                        val marksData = json.decodeFromString<Map<String, Int>>(log.marksData)
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
