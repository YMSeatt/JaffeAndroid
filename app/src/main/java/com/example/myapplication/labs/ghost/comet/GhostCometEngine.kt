package com.example.myapplication.labs.ghost.comet

import com.example.myapplication.ui.model.StudentUiItem
import java.util.ArrayDeque
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * GhostCometEngine: High-momentum activity visualization with trailing effects.
 *
 * This engine manages "Ghost Comets" that are emitted during behavior events.
 * Comets have significant momentum and leave a trailing path in the classroom's
 * social gravity field.
 *
 * BOLT Performance: Uses object pooling and manual loops to achieve zero-allocation
 * physics updates at 60fps.
 */
class GhostCometEngine {
    companion object {
        const val MAX_TRAIL_POINTS = 12
    }

    class Comet(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var life: Float = 0f,
        var colorType: Int = 0,
        val trailX: FloatArray = FloatArray(MAX_TRAIL_POINTS),
        val trailY: FloatArray = FloatArray(MAX_TRAIL_POINTS),
        var trailIndex: Int = 0
    ) {
        fun reset(x: Float, y: Float, vx: Float, vy: Float, colorType: Int) {
            this.x = x
            this.y = y
            this.vx = vx
            this.vy = vy
            this.life = 1.0f
            this.colorType = colorType
            this.trailIndex = 0
            for (i in 0 until MAX_TRAIL_POINTS) {
                trailX[i] = x
                trailY[i] = y
            }
        }

        fun updateTrail() {
            trailX[trailIndex] = x
            trailY[trailIndex] = y
            trailIndex = (trailIndex + 1) % MAX_TRAIL_POINTS
        }
    }

    private val _comets = ArrayList<Comet>(30)
    val comets: List<Comet> get() = _comets

    private val cometPool = ArrayDeque<Comet>(30)
    private val maxComets = 30
    private val canvasSize = 4000f

    /**
     * Emits a burst of comets.
     */
    fun emit(x: Float, y: Float, type: String) {
        val colorType = when {
            type.contains("Positive", ignoreCase = true) -> 0
            type.contains("Negative", ignoreCase = true) -> 1
            else -> 2
        }

        val burstCount = 2
        repeat(burstCount) {
            if (_comets.size < maxComets) {
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val speed = Random.nextFloat() * 25f + 15f

                val comet = cometPool.poll() ?: Comet()
                comet.reset(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    colorType = colorType
                )
                _comets.add(comet)
            }
        }
    }

    /**
     * Updates physics and trails.
     */
    fun update(students: List<StudentUiItem>, deltaTime: Float = 1.0f) {
        if (_comets.isEmpty()) return

        // BOLT: Hoist student positions to avoid MutableState reads in hot loop
        val studentX = FloatArray(students.size)
        val studentY = FloatArray(students.size)
        for (i in students.indices) {
            val s = students[i]
            studentX[i] = s.xPosition.value
            studentY[i] = s.yPosition.value
        }

        val iterator = _comets.iterator()
        while (iterator.hasNext()) {
            val comet = iterator.next()

            comet.updateTrail()

            // Momentum & Drag
            comet.vx *= 0.97f
            comet.vy *= 0.97f

            // Social Gravity (Attraction)
            for (i in studentX.indices) {
                val dx = studentX[i] - comet.x
                val dy = studentY[i] - comet.y
                val distSq = dx * dx + dy * dy

                // Attraction within range
                if (distSq in 400.0f..2000000.0f) {
                    val forceFactor = (8.0f / distSq) * deltaTime
                    comet.vx += dx * forceFactor
                    comet.vy += dy * forceFactor
                }
            }

            comet.x += comet.vx * deltaTime
            comet.y += comet.vy * deltaTime

            comet.life -= 0.006f * deltaTime

            // Bounce logic
            if (comet.x < 0 || comet.x > canvasSize) comet.vx *= -0.7f
            if (comet.y < 0 || comet.y > canvasSize) comet.vy *= -0.7f

            if (comet.life <= 0f) {
                iterator.remove()
                cometPool.offer(comet)
            }
        }
    }

    fun reset() {
        cometPool.addAll(_comets)
        _comets.clear()
    }
}
