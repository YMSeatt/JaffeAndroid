package com.example.myapplication.labs.ghost.stream

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * GhostStreamEngine: Synthesizes classroom events into a high-fidelity "Neural Stream".
 *
 * This engine collects and formats the most recent activities (Behavior, Quizzes, Homework)
 * into a unified list of stream entries for display in the ticker.
 */
object GhostStreamEngine {

    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    /**
     * Represents a single entry in the activity stream.
     */
    data class StreamEntry(
        val id: String,
        val timestamp: Long,
        val formattedTime: String,
        val studentName: String,
        val content: String,
        val type: EntryType
    )

    enum class EntryType {
        POSITIVE,
        NEGATIVE,
        ACADEMIC,
        SYSTEM
    }

    /**
     * Synthesizes a unified stream of recent classroom events.
     *
     * BOLT ⚡ Optimization: Uses manual index loops and pre-allocation to minimize churn
     * when processing large log lists.
     *
     * @param students The current list of students for name resolution.
     * @param behaviorLogs Recent behavior events.
     * @param quizLogs Recent quiz scores.
     * @param homeworkLogs Recent homework status updates.
     * @param maxEntries Maximum number of entries to return.
     * @return A list of [StreamEntry] objects, sorted by timestamp (newest first).
     */
    fun synthesizeStream(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>,
        maxEntries: Int = 20
    ): List<StreamEntry> {
        val studentMap = students.associateBy { it.id.toLong() }
        val entries = mutableListOf<StreamEntry>()

        // Process Behavior Logs
        for (i in behaviorLogs.indices) {
            val log = behaviorLogs[i]
            val student = studentMap[log.studentId] ?: continue
            val type = if (log.type.contains("Negative", ignoreCase = true)) EntryType.NEGATIVE else EntryType.POSITIVE
            entries.add(StreamEntry(
                id = "b_${log.timestamp}_${log.studentId}",
                timestamp = log.timestamp,
                formattedTime = TIME_FORMATTER.format(Instant.ofEpochMilli(log.timestamp)),
                studentName = student.fullName.value,
                content = log.type + (if (log.comment.isNullOrBlank()) "" else ": ${log.comment}"),
                type = type
            ))
        }

        // Process Quiz Logs
        for (i in quizLogs.indices) {
            val log = quizLogs[i]
            val student = studentMap[log.studentId] ?: continue
            entries.add(StreamEntry(
                id = "q_${log.loggedAt}_${log.studentId}",
                timestamp = log.loggedAt,
                formattedTime = TIME_FORMATTER.format(Instant.ofEpochMilli(log.loggedAt)),
                studentName = student.fullName.value,
                content = "Quiz: ${log.quizName} (${log.markValue ?: 0}/${log.maxMarkValue ?: 0})",
                type = EntryType.ACADEMIC
            ))
        }

        // Process Homework Logs
        for (i in homeworkLogs.indices) {
            val log = homeworkLogs[i]
            val student = studentMap[log.studentId] ?: continue
            entries.add(StreamEntry(
                id = "h_${log.loggedAt}_${log.studentId}",
                timestamp = log.loggedAt,
                formattedTime = TIME_FORMATTER.format(Instant.ofEpochMilli(log.loggedAt)),
                studentName = student.fullName.value,
                content = "Homework: ${log.assignmentName} -> ${log.status}",
                type = EntryType.ACADEMIC
            ))
        }

        // Sort by timestamp descending and take the limit
        return entries.sortedByDescending { it.timestamp }.take(maxEntries)
    }
}
