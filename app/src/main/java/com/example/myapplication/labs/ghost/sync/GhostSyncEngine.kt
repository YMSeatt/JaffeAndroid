package com.example.myapplication.labs.ghost.sync

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.abs
import kotlin.math.exp

/**
 * GhostSyncEngine: Calculates "Neural Synchronicity" between student nodes.
 *
 * This engine identifies students who are operating in "Sync" — sharing
 * similar behavioral rhythms, group membership, and spatial proximity.
 * It visualizes the invisible threads of collaboration in the classroom.
 */
object GhostSyncEngine {

    data class SyncLink(
        val studentA: Long,
        val studentB: Long,
        val strength: Float // 0..1
    )

    /**
     * Identifies synchronized pairs of students.
     *
     * BOLT: Optimized with manual loops and early exits.
     */
    fun calculateSyncLinks(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        timeWindowMs: Long = 10 * 60 * 1000L // 10 minutes for recent activity
    ): List<SyncLink> {
        if (students.size < 2) return emptyList()

        val currentTime = System.currentTimeMillis()
        val syncLinks = mutableListOf<SyncLink>()

        // 1. Group logs by student for O(1) access
        val logsByStudent = behaviorLogs.groupBy { it.studentId }

        // 2. Pre-calculate "Activity Score" for each student (recent log frequency)
        val activityScores = mutableMapOf<Long, Float>()
        for (student in students) {
            val studentLogs = logsByStudent[student.id.toLong()] ?: emptyList()
            var recentCount = 0
            for (log in studentLogs) {
                if (currentTime - log.timestamp < timeWindowMs) {
                    recentCount++
                } else if (log.timestamp < currentTime - timeWindowMs) {
                    break // Assumes DESC sorted logs
                }
            }
            activityScores[student.id.toLong()] = recentCount.toFloat()
        }

        // 3. Compare pairs
        for (i in students.indices) {
            for (j in i + 1 until students.size) {
                val s1 = students[i]
                val s2 = students[j]

                // Condition 1: Must be in the same group
                if (s1.groupId.value == null || s1.groupId.value != s2.groupId.value) continue

                // Condition 2: Similar activity levels
                val score1 = activityScores[s1.id.toLong()] ?: 0f
                val score2 = activityScores[s2.id.toLong()] ?: 0f
                if (score1 == 0f && score2 == 0f) continue

                val activityParity = 1f - (abs(score1 - score2) / maxOf(score1, score2, 1f))
                if (activityParity < 0.7f) continue

                // Condition 3: Spatial Proximity (Gaussian decay)
                val dx = s1.xPosition.value - s2.xPosition.value
                val dy = s1.yPosition.value - s2.yPosition.value
                val distSq = dx * dx + dy * dy
                val spatialSync = exp(-distSq / (2 * 800f * 800f))

                val totalSync = (activityParity * 0.6f + spatialSync * 0.4f)
                if (totalSync > 0.6f) {
                    syncLinks.add(SyncLink(s1.id.toLong(), s2.id.toLong(), totalSync))
                }
            }
        }

        return syncLinks.sortedByDescending { it.strength }.take(5)
    }
}
