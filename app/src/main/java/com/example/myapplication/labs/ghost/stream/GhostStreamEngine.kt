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

    /** BOLT: Lightweight holder for unsorted logs to avoid pre-mature formatting. */
    private data class PendingEntry(
        val log: Any,
        val timestamp: Long,
        val type: EntryType
    )

    /**
     * Synthesizes a unified stream of recent classroom events.
     *
     * BOLT ⚡ Optimization: Dramatically reduced complexity from O(L) to O(maxEntries).
     * 1. Leverages pre-sorted DAO inputs to only process the first [maxEntries] of each list.
     * 2. Defers expensive string formatting, ID generation, and student lookups until
     *    AFTER the final top-N items are identified.
     * 3. Uses manual index loops to eliminate iterator allocations.
     *
     * @param students The current list of students for name resolution.
     * @param behaviorLogs Recent behavior events (Pre-sorted DESC).
     * @param quizLogs Recent quiz scores (Pre-sorted DESC).
     * @param homeworkLogs Recent homework status updates (Pre-sorted DESC).
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
        if (maxEntries <= 0) return emptyList()

        // BOLT: Process only the first N items of each pre-sorted list.
        // This transforms O(B+Q+H) processing into O(maxEntries * 3) max.
        val pending = ArrayList<PendingEntry>(maxEntries * 3)

        // Behavior Logs
        val bSize = behaviorLogs.size.coerceAtMost(maxEntries)
        for (i in 0 until bSize) {
            val log = behaviorLogs[i]
            val type = if (log.type.contains("Negative", ignoreCase = true)) EntryType.NEGATIVE else EntryType.POSITIVE
            pending.add(PendingEntry(log, log.timestamp, type))
        }

        // Quiz Logs
        val qSize = quizLogs.size.coerceAtMost(maxEntries)
        for (i in 0 until qSize) {
            val log = quizLogs[i]
            pending.add(PendingEntry(log, log.loggedAt, EntryType.ACADEMIC))
        }

        // Homework Logs
        val hSize = homeworkLogs.size.coerceAtMost(maxEntries)
        for (i in 0 until hSize) {
            val log = homeworkLogs[i]
            pending.add(PendingEntry(log, log.loggedAt, EntryType.ACADEMIC))
        }

        // Sort the truncated combined list
        pending.sortByDescending { it.timestamp }
        val finalCount = pending.size.coerceAtMost(maxEntries)

        if (finalCount == 0) return emptyList()

        // BOLT: Perform expensive formatting and lookups ONLY for the items we're keeping.
        // Optimized to look up only the students actually present in the final list
        // to avoid O(N) building of a full student map.
        val result = ArrayList<StreamEntry>(finalCount)

        for (i in 0 until finalCount) {
            val p = pending[i]
            val item = p.log
            val studentId = when (item) {
                is BehaviorEvent -> item.studentId
                is QuizLog -> item.studentId
                is HomeworkLog -> item.studentId
                else -> 0L
            }

            // O(S) manual search but only called maxEntries (20) times.
            // BOLT: Replaced functional find with manual index loop to avoid iterator allocation.
            var student: StudentUiItem? = null
            for (j in 0 until students.size) {
                if (students[j].id.toLong() == studentId) {
                    student = students[j]
                    break
                }
            }
            if (student == null) continue
            val studentName = student.fullName.value
            val formattedTime = try {
                TIME_FORMATTER.format(Instant.ofEpochMilli(p.timestamp))
            } catch (e: Exception) {
                "00:00:00"
            }

            val (id, content) = when (item) {
                is BehaviorEvent -> {
                    "b_${item.timestamp}_${item.studentId}" to (item.type + (if (item.comment.isNullOrBlank()) "" else ": ${item.comment}"))
                }
                is QuizLog -> {
                    "q_${item.loggedAt}_${item.studentId}" to "Quiz: ${item.quizName} (${item.markValue ?: 0.0}/${item.maxMarkValue ?: 0.0})"
                }
                is HomeworkLog -> {
                    "h_${item.loggedAt}_${item.studentId}" to "Homework: ${item.assignmentName} -> ${item.status}"
                }
                else -> "" to ""
            }

            if (id.isNotEmpty()) {
                result.add(StreamEntry(id, p.timestamp, formattedTime, studentName, content, p.type))
            }
        }

        return result
    }
}
