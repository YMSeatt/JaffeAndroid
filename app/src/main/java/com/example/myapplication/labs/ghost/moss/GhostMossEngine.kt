package com.example.myapplication.labs.ghost.moss

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import android.util.LongSparseArray
import kotlin.math.max

/**
 * GhostMossEngine: Calculates social stagnation and dormancy metrics.
 *
 * Moss growth is a metaphor for students who haven't received attention
 * or recorded activity in a long time.
 */
object GhostMossEngine {
    private const val GROWTH_START_DAYS = 7L
    private const val MAX_GROWTH_DAYS = 21L
    private const val MS_PER_DAY = 1000L * 60 * 60 * 24

    /**
     * Calculates dormancy scores for all students.
     * Score ranges from 0.0 (Active) to 1.0 (Fully Dormant/Mossy).
     *
     * BOLT: Uses LongSparseArray and O(Recent) logic to minimize allocations.
     */
    fun calculateDormancyScores(
        students: List<Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        quizLogsByStudent: Map<Long, List<QuizLog>>,
        homeworkLogsByStudent: Map<Long, List<HomeworkLog>>,
        now: Long = System.currentTimeMillis()
    ): LongSparseArray<Float> {
        val scores = LongSparseArray<Float>(students.size)

        for (i in students.indices) {
            val student = students[i]
            val sid = student.id

            // Find the most recent activity timestamp across all log types
            var lastActivity = 0L

            val bLogs = behaviorLogsByStudent[sid]
            if (!bLogs.isNullOrEmpty()) {
                // BOLT: Logs are sorted DESC by database/ViewModel.
                // O(1) lookup of the first element instead of O(L) iteration.
                lastActivity = max(lastActivity, bLogs[0].timestamp)
            }

            val qLogs = quizLogsByStudent[sid]
            if (!qLogs.isNullOrEmpty()) {
                lastActivity = max(lastActivity, qLogs[0].timestamp)
            }

            val hLogs = homeworkLogsByStudent[sid]
            if (!hLogs.isNullOrEmpty()) {
                lastActivity = max(lastActivity, hLogs[0].loggedAt)
            }

            if (lastActivity == 0L) {
                // No activity ever recorded; treat as potentially long-term dormant.
                // For PoC, we cap it at 1.0 but maybe they are just new students.
                // Let's assume they've been here at least 7 days for moss to start.
                scores.put(sid, 0.2f)
                continue
            }

            val daysInactive = (now - lastActivity) / MS_PER_DAY

            val score = when {
                daysInactive < GROWTH_START_DAYS -> 0.0f
                daysInactive >= MAX_GROWTH_DAYS -> 1.0f
                else -> {
                    (daysInactive - GROWTH_START_DAYS).toFloat() / (MAX_GROWTH_DAYS - GROWTH_START_DAYS).toFloat()
                }
            }

            scores.put(sid, score)
        }

        return scores
    }
}
