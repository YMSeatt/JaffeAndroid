package com.example.myapplication.labs.ghost.warp

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.max

/**
 * GhostWarpEngine: Calculates spatial "curvature" points for the Ghost Warp experiment.
 */
object GhostWarpEngine {

    data class GravityPoint(
        val x: Float,
        val y: Float,
        val mass: Float,
        val radius: Float
    )

    /**
     * Identifies the top "Gravity Wells" in the classroom based on student activity.
     */
    fun calculateGravityPoints(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        maxPoints: Int = 10
    ): List<GravityPoint> {
        val currentTime = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        // Group logs by student and calculate weighted activity
        val studentActivity = behaviorLogs.groupBy { it.studentId }
            .mapValues { (_, logs) ->
                logs.sumOf { log ->
                    // Logs within the last hour have more "weight"
                    val recencyFactor = if (currentTime - log.timestamp < oneHourMs) 2.0 else 1.0
                    // Negative logs create more "warp" (turbulence)
                    val typeFactor = if (log.type.contains("Negative", ignoreCase = true)) 1.5 else 1.0
                    recencyFactor * typeFactor
                }
            }

        return students.mapNotNull { student ->
            val activity = studentActivity[student.id.toLong()] ?: return@mapNotNull null
            if (activity < 1.0) return@mapNotNull null // Threshold to create a well

            GravityPoint(
                x = student.xPosition.value,
                y = student.yPosition.value,
                mass = (activity.toFloat() * 0.1f).coerceAtMost(2.0f),
                radius = 300f + (activity.toFloat() * 50f).coerceAtMost(500f)
            )
        }
        .sortedByDescending { it.mass }
        .take(maxPoints)
    }
}
