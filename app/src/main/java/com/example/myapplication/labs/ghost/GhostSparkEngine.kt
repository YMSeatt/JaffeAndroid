package com.example.myapplication.labs.ghost

import com.example.myapplication.ui.model.StudentUiItem
import java.util.ArrayDeque
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * GhostSparkEngine: A high-performance Neural Particle System.
 *
 * This engine manages "Data Sparks" that are emitted when behavioral events occur.
 * Sparks exhibit autonomous behavior, drifting through the classroom's "Social Gravity"
 * field, providing a macroscopic view of social energy and contagion.
 */
class GhostSparkEngine {
    class Spark(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var life: Float = 0f, // 1.0 down to 0.0
        var colorType: Int = 0, // 0: Positive (Cyan), 1: Negative (Magenta), 2: Academic (Purple)
        var size: Float = 0f
    ) {
        fun reset(x: Float, y: Float, vx: Float, vy: Float, colorType: Int, size: Float) {
            this.x = x
            this.y = y
            this.vx = vx
            this.vy = vy
            this.life = 1.0f
            this.colorType = colorType
            this.size = size
        }
    }

    // BOLT: Replaced SnapshotStateList with ArrayList to eliminate Compose tracking overhead
    // for high-frequency physics objects. Redraw is triggered by the global 'time' animation.
    private val _sparks = ArrayList<Spark>(300)
    val sparks: List<Spark> get() = _sparks

    // BOLT: Object pool to eliminate O(Burst) allocations during emissions
    private val sparkPool = ArrayDeque<Spark>(300)

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

                val spark = sparkPool.poll() ?: Spark()
                spark.reset(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    colorType = colorType,
                    size = Random.nextFloat() * 12f + 4f
                )
                _sparks.add(spark)
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
        if (_sparks.isEmpty()) return

        // BOLT: Pre-fetch student positions into local arrays to avoid repeated
        // MutableState reads inside the N*S inner loop.
        val studentX = FloatArray(students.size)
        val studentY = FloatArray(students.size)
        for (i in students.indices) {
            val s = students[i]
            studentX[i] = s.xPosition.value
            studentY[i] = s.yPosition.value
        }

        val iterator = _sparks.iterator()
        while (iterator.hasNext()) {
            val spark = iterator.next()

            // 1. Apply Friction/Drag
            spark.vx *= 0.96f
            spark.vy *= 0.96f

            // 2. Social Gravity: Sparks are slightly attracted to student "energy nodes"
            for (i in studentX.indices) {
                val dx = studentX[i] - spark.x
                val dy = studentY[i] - spark.y
                val distSq = dx * dx + dy * dy

                // BOLT: Eliminate O(S*N) sqrt() by refactoring force calculation.
                // Original: force = (2.5 / dist) * dt. vx += (dx / dist) * force
                // Refactored: vx += dx * (2.5 / dist^2) * dt
                if (distSq in 400.0f..900000.0f) {
                    val forceFactor = (2.5f / distSq) * deltaTime
                    spark.vx += dx * forceFactor
                    spark.vy += dy * forceFactor
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
                sparkPool.offer(spark)
            }
        }
    }

    /**
     * Clears all active sparks.
     */
    fun reset() {
        sparkPool.addAll(_sparks)
        _sparks.clear()
    }
}
