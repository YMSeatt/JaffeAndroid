package com.example.myapplication.labs.ghost

import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * GhostSparkEngine: A high-performance Neural Particle System.
 *
 * This engine manages "Data Sparks" that are emitted when behavioral events occur.
 * Sparks exhibit autonomous behavior, drifting through the classroom's "Social Gravity"
 * field, providing a macroscopic view of social energy and contagion.
 */
class GhostSparkEngine {
    data class Spark(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float, // 1.0 down to 0.0
        val colorType: Int, // 0: Positive (Cyan), 1: Negative (Magenta), 2: Academic (Purple)
        val size: Float
    )

    private val _sparks = mutableStateListOf<Spark>()
    val sparks: List<Spark> get() = _sparks

    private val maxSparks = 300
    private val canvasSize = 4000f

    /**
     * Emits a burst of sparks at the specified coordinates.
     *
     * @param x The logical X coordinate (0..4000).
     * @param y The logical Y coordinate (0..4000).
     * @param type The behavior type string used to determine particle color.
     */
    fun emit(x: Float, y: Float, type: String) {
        val colorType = when {
            type.contains("Positive", ignoreCase = true) -> 0
            type.contains("Negative", ignoreCase = true) -> 1
            else -> 2
        }

        val burstCount = 15
        repeat(burstCount) {
            if (_sparks.size < maxSparks) {
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val speed = Random.nextFloat() * 15f + 5f
                _sparks.add(
                    Spark(
                        x = x,
                        y = y,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        life = 1.0f,
                        colorType = colorType,
                        size = Random.nextFloat() * 12f + 4f
                    )
                )
            }
        }
    }

    /**
     * Updates the physics and life cycle of all active sparks.
     *
     * @param students The current list of students to calculate social gravity.
     * @param deltaTime Scaling factor for physics (default 1.0 for 60fps).
     */
    fun update(students: List<StudentUiItem>, deltaTime: Float = 1.0f) {
        val iterator = _sparks.iterator()
        while (iterator.hasNext()) {
            val spark = iterator.next()

            // 1. Apply Friction/Drag
            spark.vx *= 0.96f
            spark.vy *= 0.96f

            // 2. Social Gravity: Sparks are slightly attracted to student "energy nodes"
            students.forEach { student ->
                val dx = student.xPosition.value - spark.x
                val dy = student.yPosition.value - spark.y
                val distSq = dx * dx + dy * dy

                // Avoid singularity and apply range limit
                if (distSq in 400.0f..900000.0f) {
                    val dist = sqrt(distSq)
                    // Inverse linear attraction
                    val force = (2.5f / dist) * deltaTime
                    spark.vx += (dx / dist) * force
                    spark.vy += (dy / dist) * force
                }
            }

            // 3. Movement
            spark.x += spark.vx * deltaTime
            spark.y += spark.vy * deltaTime

            // 4. Life Decay
            spark.life -= 0.008f * deltaTime

            // 5. Bounds Check (Soft Wrap/Bounce)
            if (spark.x < 0 || spark.x > canvasSize) spark.vx *= -0.5f
            if (spark.y < 0 || spark.y > canvasSize) spark.vy *= -0.5f

            if (spark.life <= 0f) {
                iterator.remove()
            }
        }
    }

    /**
     * Clears all active sparks.
     */
    fun reset() {
        _sparks.clear()
    }
}
