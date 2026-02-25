package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.max
import kotlin.math.min

/**
 * GhostNebulaEngine: Atmospheric analysis engine for the "Ghost Nebula" experiment.
 *
 * It processes behavior logs and student positions to identify activity "clusters"
 * that drive the gaseous visualization.
 */
object GhostNebulaEngine {

    /**
     * Represents an activity cluster in the nebula.
     */
    data class NebulaCluster(
        val x: Float,
        val y: Float,
        val density: Float,
        val colorIndex: Float // 0: Positive, 1: Negative, 2: Neutral
    )

    /**
     * Analyzes the classroom state to generate nebula parameters.
     */
    fun calculateNebula(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        timeWindowMs: Long = 15 * 60 * 1000L // 15 minutes for real-time vibe
    ): Pair<Float, List<NebulaCluster>> {
        if (students.isEmpty()) return 0.2f to emptyList()

        val currentTime = System.currentTimeMillis()
        val recentLogs = behaviorLogs.filter { it.timestamp > currentTime - timeWindowMs }

        if (recentLogs.isEmpty()) return 0.3f to emptyList()

        val studentMap = students.associateBy { it.id.toLong() }
        val clusters = mutableListOf<NebulaCluster>()

        // Group logs by student to identify hotspots
        val logsByStudent = recentLogs.groupBy { it.studentId }

        val sortedStudents = logsByStudent.entries.sortedByDescending { it.value.size }
            .take(10) // AGSL shader supports up to 10 clusters in our PoC

        sortedStudents.forEach { (studentId, logs) ->
            val student = studentMap[studentId] ?: return@forEach

            val positiveCount = logs.count { it.type.contains("Positive", ignoreCase = true) }
            val negativeCount = logs.count { it.type.contains("Negative", ignoreCase = true) }

            val colorIndex = when {
                positiveCount > negativeCount -> 0f
                negativeCount > positiveCount -> 1f
                else -> 2f
            }

            val density = (logs.size.toFloat() / 5.0f).coerceIn(0.1f, 1.0f)

            clusters.add(
                NebulaCluster(
                    x = student.xPosition.value,
                    y = student.yPosition.value,
                    density = density,
                    colorIndex = colorIndex
                )
            )
        }

        val globalIntensity = (recentLogs.size.toFloat() / 20f + 0.3f).coerceIn(0.3f, 1.0f)

        return globalIntensity to clusters
    }
}
