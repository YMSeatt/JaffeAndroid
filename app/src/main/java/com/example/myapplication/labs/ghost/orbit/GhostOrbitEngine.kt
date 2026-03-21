package com.example.myapplication.labs.ghost.orbit

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.*

/**
 * GhostOrbitEngine: Simulates a "Classroom Galaxy" where student engagement
 * translates into orbital dynamics.
 *
 * Metrics:
 * - **Speed**: Driven by the frequency of recent logs (Engagement).
 * - **Radius**: Driven by the balance of positive vs. negative logs (Stability).
 * - **Social Gravity**: Students with high positive reinforcement act as attractors.
 */
object GhostOrbitEngine {

    /**
     * Represents the orbital state of a student node.
     */
    data class OrbitalState(
        val studentId: Long,
        val x: Float,
        val y: Float,
        val centerX: Float,
        val centerY: Float,
        val angle: Float,      // Current orbital angle
        val speed: Float,      // Angular velocity
        val radius: Float,     // Orbital radius from center
        val energy: Float,     // Overall "Energy" level for visual effects
        val stability: Float   // 0.0 (Unstable/Chaos) to 1.0 (Harmonic)
    )

    /**
     * Calculates orbital parameters for all students.
     *
     * @param students List of students and their logical positions.
     * @param behaviorLogs Historical logs for analysis.
     * @param time Elapsed time for animation updates.
     * @return A list of [OrbitalState] objects.
     */
    fun calculateOrbits(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        time: Float
    ): List<OrbitalState> {
        if (students.isEmpty()) return emptyList()

        val logsByStudent = behaviorLogs.groupBy { it.studentId }
        val currentTime = System.currentTimeMillis()
        val windowMillis = 3600_000L // 1 hour window for engagement

        // Identify "Social Suns" (High positive engagement influencers)
        val suns = students.filter { student ->
            val logs = logsByStudent[student.id.toLong()] ?: emptyList()
            logs.count { !it.type.contains("Negative", ignoreCase = true) } > 5
        }

        return students.map { student ->
            val studentId = student.id.toLong()
            val logs = logsByStudent[studentId] ?: emptyList()
            val recentLogs = logs.filter { currentTime - it.timestamp < windowMillis }

            val posCount = logs.count { !it.type.contains("Negative", ignoreCase = true) }
            val negCount = logs.count { it.type.contains("Negative", ignoreCase = true) }
            val totalCount = logs.size

            // Engagement drives Speed
            val engagement = (recentLogs.size.toFloat() / 5f).coerceIn(0.1f, 2.0f)
            val baseSpeed = 0.5f * engagement

            // Stability drives Radius (Harmonic students stay closer to the "Core")
            val stability = if (totalCount > 0) {
                (posCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
            } else 0.8f

            val baseRadius = 150f + (1.0f - stability) * 300f

            // Energy for visual pulse/glow
            val energy = (totalCount.toFloat() / 10f).coerceIn(0.2f, 1.0f)

            // Calculate orbital position based on center of layout or nearest Sun
            val center = if (suns.isNotEmpty() && !suns.all { it.id == student.id }) {
                val nearestSun = suns.filter { it.id != student.id }.minByOrNull { sun ->
                    val dx = sun.xPosition.value - student.xPosition.value
                    val dy = sun.yPosition.value - student.yPosition.value
                    dx * dx + dy * dy
                }!!
                nearestSun.xPosition.value to nearestSun.yPosition.value
            } else {
                2000f to 2000f // Classroom logical center
            }

            val angle = (time * baseSpeed + (studentId * 0.785f)) % (2f * PI.toFloat())
            val orbitalX = center.first + cos(angle) * baseRadius
            val orbitalY = center.second + sin(angle) * baseRadius

            OrbitalState(
                studentId = studentId,
                x = orbitalX,
                y = orbitalY,
                centerX = center.first,
                centerY = center.second,
                angle = angle,
                speed = baseSpeed,
                radius = baseRadius,
                energy = energy,
                stability = stability
            )
        }
    }
}
