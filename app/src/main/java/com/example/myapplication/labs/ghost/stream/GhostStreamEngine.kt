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
 *
 * ### Architectural Role:
 * The StreamEngine acts as the central aggregator for the Ghost Stream feature. It transforms
 * disparate database entities into a common [StreamEntry] format, enabling a consistent
 * chronological view of classroom activity.
 */
object GhostStreamEngine {

    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    /**
     * Represents a single entry in the activity stream.
     *
     * @property id Unique identifier for the entry (e.g., "b_timestamp_studentId").
     * @property timestamp Absolute epoch timestamp of the event.
     * @property formattedTime Human-readable time string (HH:mm:ss).
     * @property studentName The display name of the student associated with the event.
     * @property content Detailed description of the event (e.g., behavior type or quiz score).
     * @property type Categorization used for visual styling in the UI.
     */
    data class StreamEntry(
        val id: String,
        val timestamp: Long,
        val formattedTime: String,
        val studentName: String,
        val content: String,
        val type: EntryType
    )

    /**
     * Defines the visual and logical categories for stream entries.
     */
    enum class EntryType {
        /** Positive behavioral reinforcement. */
        POSITIVE,
        /** Negative behavioral friction. */
        NEGATIVE,
        /** Academic assessments and homework updates. */
        ACADEMIC,
        /** Administrative or system-level milestones. */
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
     * ### BOLT ⚡ Performance Optimization:
     * This method is designed to maintain 60fps UI performance by achieving $O(\text{maxEntries})$
     * complexity regardless of the total number of logs in the database.
     *
     * 1. **Early Truncation**: Leverages pre-sorted DAO inputs to only process the first [maxEntries]
     *    of each log list.
     * 2. **Deferred Work**: Defers expensive string formatting, ID generation, and student lookups
     *    until *after* the final combined top-N items are identified.
     * 3. **Memory Efficiency**: Uses manual index loops to eliminate [Iterator] allocations
     *    and [android.util.LongSparseArray] for $O(1)$ student resolution.
     *
     * @param students The current list of students for name resolution.
     * @param behaviorLogs Recent behavior events (Must be pre-sorted DESC).
     * @param quizLogs Recent quiz scores (Must be pre-sorted DESC).
     * @param homeworkLogs Recent homework status updates (Must be pre-sorted DESC).
     * @param maxEntries Maximum number of entries to return in the final stream.
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
        // BOLT: Pre-index the students into a LongSparseArray for O(1) lookup,
        // transforming O(maxEntries * S) into O(S + maxEntries).
        val studentMap = android.util.LongSparseArray<StudentUiItem>(students.size)
        for (i in students.indices) {
            val s = students[i]
            studentMap.put(s.id.toLong(), s)
        }

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

            val student = studentMap.get(studentId)
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
