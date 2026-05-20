package com.example.myapplication.labs.ghost.sync

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.util.GhostActivityMetrics
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

    /** BOLT: Distance threshold for social synchronicity on the 4000x4000 canvas. */
    private const val SYNC_DISTANCE_THRESHOLD = 800f
    private const val SYNC_DISTANCE_THRESHOLD_SQ = SYNC_DISTANCE_THRESHOLD * SYNC_DISTANCE_THRESHOLD

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

        val logsByStudent = behaviorLogs.groupBy { it.studentId }
        return calculateSyncLinks(
            students = students,
            behaviorLogsByStudent = logsByStudent,
            timeWindowMs = timeWindowMs
        )
    }

    /**
     * BOLT: High-performance overload utilizing pre-grouped logs and index-based group comparisons.
     * Transforms O(S^2) into O(Sum(Gi^2)) by grouping students by group ID first.
     */
    fun calculateSyncLinks(
        students: List<StudentUiItem>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        timeWindowMs: Long = 600_000L
    ): List<SyncLink> {
        if (students.size < 2) return emptyList()

        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - timeWindowMs

        // 1. Group students by groupId to avoid O(S^2) comparisons
        val studentsByGroup = mutableMapOf<Long, MutableList<StudentUiItem>>()
        for (i in students.indices) {
            val student = students[i]
            val gid = student.groupId.value ?: continue
            studentsByGroup.getOrPut(gid) { mutableListOf() }.add(student)
        }

        // 2. Pre-calculate "Activity Score" for each student (recent log frequency)
        val activityScores = mutableMapOf<Long, Float>()
        for (i in students.indices) {
            val student = students[i]
            val studentLogs = behaviorLogsByStudent[student.id.toLong()] ?: continue
            var recentCount = 0
            for (j in studentLogs.indices) {
                val log = studentLogs[j]
                if (log.timestamp >= startTime) {
                    recentCount++
                } else {
                    break // Assumes DESC sorted logs
                }
            }
            if (recentCount > 0) {
                activityScores[student.id.toLong()] = recentCount.toFloat()
            }
        }

        val syncLinks = mutableListOf<SyncLink>()

        // 3. Compare pairs within groups
        for (groupEntry in studentsByGroup) {
            val groupStudents = groupEntry.value
            if (groupStudents.size < 2) continue

            for (i in groupStudents.indices) {
                val s1 = groupStudents[i]
                val score1 = activityScores[s1.id.toLong()] ?: 0f
                if (score1 == 0f) continue

                for (j in i + 1 until groupStudents.size) {
                    val s2 = groupStudents[j]
                    val score2 = activityScores[s2.id.toLong()] ?: 0f
                    if (score2 == 0f) continue

                    // Similar activity levels
                    val activityParity = 1f - (abs(score1 - score2) / maxOf(score1, score2, 1f))
                    if (activityParity < 0.7f) continue

                    // Spatial Proximity (Gaussian decay)
                    val dx = s1.xPosition.value - s2.xPosition.value
                    val dy = s1.yPosition.value - s2.yPosition.value
                    val distSq = dx * dx + dy * dy
                    val spatialSync = exp(-distSq / (2 * SYNC_DISTANCE_THRESHOLD_SQ))

                    val totalSync = (activityParity * 0.6f + spatialSync * 0.4f)
                    if (totalSync > 0.6f) {
                        syncLinks.add(SyncLink(s1.id.toLong(), s2.id.toLong(), totalSync))
                    }
                }
            }
        }

        return syncLinks
    }

    /**
     * BOLT: High-performance overload for Student entities utilizing centralized activity metrics.
     * Removes redundant O(L) temporal scanning during seating chart interactions.
     *
     * @param students The list of student entities.
     * @param activityMetrics Pre-calculated activity metrics for students.
     * @return A list of identified [SyncLink]s.
     */
    fun calculateSyncLinksForStudents(
        students: List<com.example.myapplication.data.Student>,
        activityMetrics: Map<Long, GhostActivityMetrics>
    ): List<SyncLink> {
        if (students.size < 2) return emptyList()

        // 1. Group students by groupId to avoid O(S^2) comparisons
        val studentsByGroup = mutableMapOf<Long, MutableList<com.example.myapplication.data.Student>>()
        for (i in students.indices) {
            val student = students[i]
            val gid = student.groupId ?: continue
            studentsByGroup.getOrPut(gid) { mutableListOf() }.add(student)
        }

        val syncLinks = mutableListOf<SyncLink>()

        // 2. Compare pairs within groups using pre-calculated activity intensity
        for (groupEntry in studentsByGroup) {
            val groupStudents = groupEntry.value
            if (groupStudents.size < 2) continue

            for (i in groupStudents.indices) {
                val s1 = groupStudents[i]
                val metrics1 = activityMetrics[s1.id] ?: continue
                val score1 = metrics1.recentCount.toFloat()

                for (j in i + 1 until groupStudents.size) {
                    val s2 = groupStudents[j]
                    val metrics2 = activityMetrics[s2.id] ?: continue
                    val score2 = metrics2.recentCount.toFloat()

                    val activityParity = 1f - (abs(score1 - score2) / maxOf(score1, score2, 1f))
                    if (activityParity < 0.7f) continue

                    val dx = s1.xPosition - s2.xPosition
                    val dy = s1.yPosition - s2.yPosition
                    val distSq = dx * dx + dy * dy
                    val spatialSync = exp(-distSq / (2 * SYNC_DISTANCE_THRESHOLD_SQ))

                    val totalSync = (activityParity * 0.6f + spatialSync * 0.4f)
                    if (totalSync > 0.6f) {
                        syncLinks.add(SyncLink(s1.id, s2.id, totalSync))
                    }
                }
            }
        }

        return syncLinks
    }

    /**
     * Legacy structure for compatibility. Prefer the activityMetrics overload.
     */
    fun calculateSyncLinksForStudents(
        students: List<com.example.myapplication.data.Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        timeWindowMs: Long = 600_000L
    ): List<SyncLink> {
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - timeWindowMs
        val activityMap = mutableMapOf<Long, GhostActivityMetrics>()

        for (student in students) {
            val logs = behaviorLogsByStudent[student.id] ?: continue
            var recentCount = 0
            for (log in logs) {
                if (log.timestamp < startTime) break
                recentCount++
            }
            if (recentCount > 0) {
                activityMap[student.id] = GhostActivityMetrics(
                    studentId = student.id, posCount = 0, negCount = 0, recentCount = recentCount,
                    intensity = 0f, polarity = 0f
                )
            }
        }
        return calculateSyncLinksForStudents(students, activityMap)
    }
}
