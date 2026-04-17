package com.example.myapplication.labs.ghost.orbit

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.*

/**
 * GhostOrbitEngine: Simulates a "Classroom Galaxy" where student engagement
 * translates into orbital dynamics.
 *
 * This engine maps behavioral interactions to gravitational forces, creating
 * a procedural solar system out of classroom data.
 *
 * ### Orbital Physics:
 * - **Speed**: Driven by the frequency of recent logs (Engagement). High-activity
 *   students exhibit higher angular velocity.
 * - **Radius**: Driven by log polarity (Stability). Students with high positive
 *   ratios stay closer to the "Core" or their assigned "Social Sun".
 * - **Social Gravity**: Students with significant positive history (5+ logs) act
 *   as gravitational attractors, pulling others into their orbital field.
 */
object GhostOrbitEngine {

    /**
     * Represents the immutable orbital parameters for a student,
     * calculated only when data changes.
     */
    data class OrbitalParameters(
        val studentId: Long,
        val centerX: Float,
        val centerY: Float,
        val speed: Float,      // Angular velocity
        val radius: Float,     // Orbital radius from center
        val energy: Float,     // Overall "Energy" level for visual effects
        val stability: Float   // 0.0 (Unstable/Chaos) to 1.0 (Harmonic)
    )

    /**
     * Represents the time-dependent orbital state of a student node.
     */
    data class OrbitalState(
        val studentId: Long,
        val x: Float,
        val y: Float,
        val centerX: Float,
        val centerY: Float,
        val angle: Float,
        val speed: Float,
        val radius: Float,
        val energy: Float,
        val stability: Float
    )

    /**
     * BOLT: Calculates the heavy orbital parameters (grouping, sun-identification)
     * only when data changes.
     */
    fun calculateOrbitalParameters(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>
    ): List<OrbitalParameters> {
        val logsByStudent = behaviorLogs.groupBy { it.studentId }
        return calculateOrbitalParameters(students, logsByStudent)
    }

    /**
     * BOLT: Optimized overload accepting pre-grouped logs to avoid O(L) grouping.
     * Uses manual index loops and pre-allocated buffers to minimize allocations.
     */
    fun calculateOrbitalParameters(
        students: List<StudentUiItem>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>
    ): List<OrbitalParameters> {
        if (students.isEmpty()) return emptyList()

        val currentTime = System.currentTimeMillis()
        val windowMillis = 3600_000L

        // Identify "Social Suns" - O(S)
        val sunCountThreshold = 5
        val sunsX = FloatArray(students.size)
        val sunsY = FloatArray(students.size)
        val sunsIds = IntArray(students.size)
        var sunCount = 0

        for (i in students.indices) {
            val student = students[i]
            val logs = behaviorLogsByStudent[student.id.toLong()] ?: continue

            var posCount = 0
            for (j in logs.indices) {
                if (!logs[j].type.contains("Negative", ignoreCase = true)) {
                    posCount++
                }
            }
            if (posCount > sunCountThreshold) {
                sunsX[sunCount] = student.xPosition.value
                sunsY[sunCount] = student.yPosition.value
                sunsIds[sunCount] = student.id
                sunCount++
            }
        }

        val results = ArrayList<OrbitalParameters>(students.size)

        for (i in students.indices) {
            val student = students[i]
            val studentId = student.id.toLong()
            val logs = behaviorLogsByStudent[studentId] ?: emptyList()

            // Single-pass metrics calculation
            var recentCount = 0
            var posCount = 0
            var negCount = 0
            for (j in logs.indices) {
                val log = logs[j]
                if (currentTime - log.timestamp < windowMillis) {
                    recentCount++
                }
                if (log.type.contains("Negative", ignoreCase = true)) {
                    negCount++
                } else {
                    posCount++
                }
            }
            val totalCount = logs.size

            // Engagement drives Speed
            val engagement = (recentCount.toFloat() / 5f).coerceIn(0.1f, 2.0f)
            val baseSpeed = 0.5f * engagement

            // Stability drives Radius
            val stability = if (totalCount > 0) {
                (posCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
            } else 0.8f

            val baseRadius = 150f + (1.0f - stability) * 300f

            // Energy for visual pulse/glow
            val energy = (totalCount.toFloat() / 10f).coerceIn(0.2f, 1.0f)

            // Calculate center based on nearest Sun
            var centerX = 2000f
            var centerY = 2000f

            if (sunCount > 0) {
                var minDistSq = Float.MAX_VALUE
                val sX = student.xPosition.value
                val sY = student.yPosition.value

                for (j in 0 until sunCount) {
                    if (sunsIds[j] == student.id) continue
                    val dx = sunsX[j] - sX
                    val dy = sunsY[j] - sY
                    val d2 = dx * dx + dy * dy
                    if (d2 < minDistSq) {
                        minDistSq = d2
                        centerX = sunsX[j]
                        centerY = sunsY[j]
                    }
                }
            }

            results.add(OrbitalParameters(
                studentId = studentId,
                centerX = centerX,
                centerY = centerY,
                speed = baseSpeed,
                radius = baseRadius,
                energy = energy,
                stability = stability
            ))
        }

        return results
    }

    /**
     * BOLT: Lightweight position update based on pre-calculated parameters and time.
     * This can safely run at 60fps.
     */
    fun updateOrbitalStates(
        params: List<OrbitalParameters>,
        time: Float
    ): List<OrbitalState> {
        return params.map { p ->
            val angle = (time * p.speed + (p.studentId * 0.785f)) % (2f * PI.toFloat())
            val orbitalX = p.centerX + cos(angle) * p.radius
            val orbitalY = p.centerY + sin(angle) * p.radius

            OrbitalState(
                studentId = p.studentId,
                x = orbitalX,
                y = orbitalY,
                centerX = p.centerX,
                centerY = p.centerY,
                angle = angle,
                speed = p.speed,
                radius = p.radius,
                energy = p.energy,
                stability = p.stability
            )
        }
    }

    /**
     * Kept for compatibility, but internally uses the optimized pipeline.
     */
    fun calculateOrbits(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        time: Float
    ): List<OrbitalState> {
        val params = calculateOrbitalParameters(students, behaviorLogs)
        return updateOrbitalStates(params, time)
    }
}
