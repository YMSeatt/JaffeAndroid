package com.example.myapplication.labs.ghost.emergence

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostEmergenceEngine: A cellular automata simulation for "Behavioral Emergence".
 *
 * It treats the classroom as a 10x10 grid (optimized for GPU uniform limits)
 * where "Vitality" emerges and diffuses based on student logs.
 */
class GhostEmergenceEngine {
    companion object {
        const val GRID_SIZE = 10
        const val CANVAS_SIZE = 4000f
        private const val DECAY_RATE = 0.95f
        private const val DIFFUSION_RATE = 0.1f
    }

    private var vitalityGrid = FloatArray(GRID_SIZE * GRID_SIZE)

    /**
     * Updates the emergence simulation based on the latest data.
     */
    fun update(
        students: List<StudentUiItem>,
        events: List<BehaviorEvent>,
        deltaTime: Float = 1.0f
    ): FloatArray {
        val nextGrid = FloatArray(GRID_SIZE * GRID_SIZE)
        val currentTime = System.currentTimeMillis()
        val timeWindow = 24 * 60 * 60 * 1000L // 24 hours

        // 1. Process Behavior Events into temporary impulse grid
        val impulses = FloatArray(GRID_SIZE * GRID_SIZE)
        val studentMap = students.associateBy { it.id.toLong() }

        events.filter { it.timestamp > currentTime - timeWindow }.forEach { event ->
            val student = studentMap[event.studentId] ?: return@forEach
            val gx = (student.xPosition.value / CANVAS_SIZE * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val gy = (student.yPosition.value / CANVAS_SIZE * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val idx = gy * GRID_SIZE + gx

            val weight = when {
                event.type.contains("Positive", ignoreCase = true) -> 0.15f
                event.type.contains("Negative", ignoreCase = true) -> -0.2f
                else -> 0.05f
            }
            impulses[idx] += weight
        }

        // 2. Apply Cellular Automata Rules (Decay, Diffusion, Impulse)
        for (y in 0 until GRID_SIZE) {
            for (x in 0 until GRID_SIZE) {
                val idx = y * GRID_SIZE + x

                // Neighbor average for diffusion
                var sum = 0f
                var count = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        val nx = x + dx
                        val ny = y + dy
                        if (nx in 0 until GRID_SIZE && ny in 0 until GRID_SIZE) {
                            sum += vitalityGrid[ny * GRID_SIZE + nx]
                            count++
                        }
                    }
                }
                val neighborAvg = if (count > 0) sum / count else 0f

                // State Transition
                val current = vitalityGrid[idx]
                val diffused = current + (neighborAvg - current) * DIFFUSION_RATE
                val decayed = diffused * DECAY_RATE
                val withImpulse = decayed + impulses[idx]

                nextGrid[idx] = withImpulse.coerceIn(-1.0f, 1.0f)
            }
        }

        vitalityGrid = nextGrid
        return vitalityGrid
    }

    fun getGrid() = vitalityGrid
}
