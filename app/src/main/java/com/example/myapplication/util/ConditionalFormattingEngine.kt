@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
package com.example.myapplication.util

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

// Data classes to represent the JSON structure in ConditionalFormattingRule
@Serializable
data class ActiveTime(
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("days_of_week") val daysOfWeek: List<Int>
)

@Serializable
data class Condition(
    val type: String,
    @SerialName("group_id") val groupId: Long? = null,
    @SerialName("behavior_name") val behaviorName: String? = null,
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

@Serializable
data class Format(
    val color: String? = null,
    val outline: String? = null
)

object ConditionalFormattingEngine {

    private val json = Json { ignoreUnknownKeys = true }

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
        val matchingFormats = mutableListOf<Pair<String?, String?>>()

        // Sort rules by priority
        val sortedRules = rules.sortedBy { it.priority }

        for (rule in sortedRules) {
            try {
                val condition = json.decodeFromString<Condition>(rule.conditionJson)
                val format = json.decodeFromString<Format>(rule.formatJson)

                if (checkCondition(
                        student,
                        condition,
                        behaviorLog,
                        quizLog,
                        homeworkLog,
                        isLiveQuizActive,
                        liveQuizScores,
                        isLiveHomeworkActive,
                        liveHomeworkScores,
                        currentMode
                    )
                ) {
                    matchingFormats.add(Pair(format.color, format.outline))
                }
            } catch (e: Exception) {
                // Log error or handle gracefully
                println("Error processing rule ${rule.id}: ${e.message}")
            }
        }

        return matchingFormats
    }

    private fun checkCondition(
        student: StudentDetailsForDisplay,
        condition: Condition,
        behaviorLog: List<BehaviorEvent>,
        quizLog: List<QuizLog>,
        homeworkLog: List<HomeworkLog>,
        isLiveQuizActive: Boolean,
        liveQuizScores: Map<Long, Map<String, Any>>,
        isLiveHomeworkActive: Boolean,
        liveHomeworkScores: Map<Long, Map<String, Any>>,
        currentMode: String
    ): Boolean {
        // Check active_modes
        condition.activeModes?.let { activeModes ->
            if (activeModes.isNotEmpty() && currentMode !in activeModes) {
                return false
            }
        }

        // Check active_times
        condition.activeTimes?.let { activeTimes ->
            if (activeTimes.isNotEmpty()) {
                val now = java.util.Calendar.getInstance()
                val dayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK) // Sunday = 1, Saturday = 7
                val currentTime = String.format("%02d:%02d", now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE))

                val isTimeValid = activeTimes.any { activeTime ->
                    val start = activeTime.startTime
                    val end = activeTime.endTime
                    val days = activeTime.daysOfWeek

                    // Python: Monday is 0 and Sunday is 6
                    // Java: Sunday is 1 and Saturday is 7
                    val javaDayOfWeek = if (dayOfWeek == 1) 6 else dayOfWeek - 2

                    currentTime in start..end && javaDayOfWeek in days
                }

                if (!isTimeValid) {
                    return false
                }
            }
        }

        return when (condition.type) {
            "group" -> student.groupId == condition.groupId
            "behavior_count" -> {
                val behaviorName = condition.behaviorName ?: return false
                val countThreshold = condition.countThreshold ?: return false
                val timeWindowHours = condition.timeWindowHours ?: 24
                val cutoffTime = System.currentTimeMillis() - timeWindowHours * 60 * 60 * 1000

                val count = behaviorLog.count {
                    it.studentId == student.id.toLong() &&
                    it.type.equals(behaviorName, ignoreCase = true) &&
                    it.timestamp >= cutoffTime
                }
                count >= countThreshold
            }
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
                        val percentage = (score / maxScore) * 100
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
            "live_quiz_response" -> {
                if (!isLiveQuizActive) return false
                val studentScores = liveQuizScores[student.id.toLong()] ?: return false
                val lastResponse = studentScores["last_response"] as? String ?: return false
                lastResponse.equals(condition.quizResponse, ignoreCase = true)
            }
            "live_homework_yes_no" -> {
                if (!isLiveHomeworkActive) return false
                val studentScores = liveHomeworkScores[student.id.toLong()] ?: return false
                val homeworkTypeId = condition.homeworkTypeId ?: return false
                val homeworkResponse = condition.homeworkResponse ?: return false
                val studentResponse = studentScores[homeworkTypeId] as? String ?: return false
                studentResponse.equals(homeworkResponse, ignoreCase = true)
            }
            "live_homework_select" -> {
                if (!isLiveHomeworkActive) return false
                val studentScores = liveHomeworkScores[student.id.toLong()] ?: return false
                val homeworkOptionName = condition.homeworkOptionName ?: return false
                val selectedOptions = studentScores["selected_options"] as? List<*> ?: return false
                selectedOptions.any { it.toString().equals(homeworkOptionName, ignoreCase = true) }
            }
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
