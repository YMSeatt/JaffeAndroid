package com.example.myapplication.labs.ghost.osmosis

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * GhostOsmosisEngine: Calculates Knowledge Diffusion and Behavioral Concentration.
 *
 * This engine models the classroom as a fluid field where academic performance
 * and behavioral patterns diffuse between students based on proximity.
 */
object GhostOsmosisEngine {
    private const val CANVAS_SIZE = 4000f
    private const val DIFFUSION_RADIUS = 1000f // Effective radius for osmosis

    data class OsmoticNode(
        val id: Long,
        val x: Float,
        val y: Float,
        val knowledgePotential: Float, // 0..1 (Academic strength)
        val behaviorConcentration: Float // -1..1 (Negative to Positive behavior)
    )

    data class DiffusionGradient(
        val x: Float,
        val y: Float,
        val potential: Float,
        val color: Triple<Float, Float, Float>
    )

    fun calculateOsmosis(
        students: List<OsmoticNode>,
        gridSize: Int = 20
    ): List<DiffusionGradient> {
        val gradients = mutableListOf<DiffusionGradient>()
        val step = CANVAS_SIZE / gridSize

        for (iy in 0 until gridSize) {
            for (ix in 0 until gridSize) {
                val gx = ix * step + step / 2f
                val gy = iy * step + step / 2f

                var totalKnowledge = 0f
                var totalBehavior = 0f
                var totalWeight = 0f

                students.forEach { student ->
                    val dx = gx - student.x
                    val dy = gy - student.y
                    val dist = sqrt(dx * dx + dy * dy)

                    if (dist < DIFFUSION_RADIUS) {
                        // Gaussian decay for diffusion weight
                        val weight = exp(-(dist * dist) / (2 * 400f * 400f))
                        totalKnowledge += student.knowledgePotential * weight
                        totalBehavior += student.behaviorConcentration * weight
                        totalWeight += weight
                    }
                }

                if (totalWeight > 0) {
                    val avgK = (totalKnowledge / totalWeight).coerceIn(0f, 1f)
                    val avgB = (totalBehavior / totalWeight).coerceIn(-1f, 1f)

                    // Color mapping:
                    // Knowledge -> Blue/Cyan intensity
                    // Positive Behavior -> Green
                    // Negative Behavior -> Red
                    val r = if (avgB < 0) -avgB else 0f
                    val g = if (avgB > 0) avgB else 0f
                    val b = avgK

                    gradients.add(
                        DiffusionGradient(
                            x = gx,
                            y = gy,
                            potential = (avgK + avgB.let { if (it > 0) it else -it }) / 2f,
                            color = Triple(r, g, b)
                        )
                    )
                }
            }
        }
        return gradients
    }

    fun calculateStudentPotentials(
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): Pair<Float, Float> {
        val kPotential = if (quizLogs.isEmpty() && homeworkLogs.isEmpty()) 0.5f else {
            val qAvg = if (quizLogs.isNotEmpty()) {
                quizLogs.mapNotNull { it.markValue?.let { v -> it.maxMarkValue?.let { m -> v / m } } }.average().toFloat()
            } else 0.5f

            val hAvg = if (homeworkLogs.isNotEmpty()) {
                homeworkLogs.count { it.status.contains("Done", ignoreCase = true) }.toFloat() / homeworkLogs.size
            } else 0.5f

            (qAvg + hAvg) / 2f
        }

        val bConcentration = if (behaviorLogs.isEmpty()) 0f else {
            val pos = behaviorLogs.count { !it.type.contains("Negative", ignoreCase = true) }
            val neg = behaviorLogs.count { it.type.contains("Negative", ignoreCase = true) }
            (pos - neg).toFloat() / behaviorLogs.size.coerceAtLeast(1)
        }

        return Pair(kPotential.coerceIn(0f, 1f), bConcentration.coerceIn(-1f, 1f))
    }
}
