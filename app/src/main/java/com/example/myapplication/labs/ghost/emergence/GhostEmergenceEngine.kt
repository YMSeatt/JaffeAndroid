package com.example.myapplication.labs.ghost.emergence

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostEmergenceEngine: A cellular automata simulation for "Behavioral Emergence".
 *
 * It treats the classroom as a 10x10 grid (optimized for GPU uniform limits)
 * where "Vitality" emerges and diffuses based on student logs.
 *
 * ### Simulation Rules:
 * 1. **Impulse**: Behavioral events act as local energy injections (Positive = +0.15f,
 *    Negative = -0.2f).
 * 2. **Diffusion**: Vitality flows to neighboring grid cells at a 0.1f rate,
 *    simulating social contagion.
 * 3. **Decay**: Grid energy naturally dissipates at a 0.95f rate per update cycle.
 *
 * ### Optimization:
 * The 10x10 grid size is chosen specifically to remain within standard AGSL uniform array
 * limits (usually 64-128 floats) while providing enough resolution to represent classroom
 * spatial dynamics.
 */
class GhostEmergenceEngine {
    companion object {
        /** The dimensions of the square vitality grid. */
        const val GRID_SIZE = 10
        /** The logical coordinate size of the seating chart. */
        const val CANVAS_SIZE = 4000f
        /** The rate at which energy dissipates in each cycle. */
        private const val DECAY_RATE = 0.95f
        /** The rate at which energy flows between neighboring cells. */
        private const val DIFFUSION_RATE = 0.1f
    }

    private var vitalityGrid = FloatArray(GRID_SIZE * GRID_SIZE)

    /**
     * Encapsulates the results of a grid-wide emergence analysis.
     *
     * @property posCount Number of grid cells with vitality above the 0.1 threshold.
     * @property negCount Number of grid cells with vitality below the -0.1 threshold.
     * @property peakVitality The maximum vitality value found in the grid.
     * @property voidVitality The minimum (most negative) vitality value found in the grid.
     * @property status A descriptive string indicating "Growth Dominant" or "Decay Warning".
     */
    data class EmergenceAnalysis(
        val posCount: Int,
        val negCount: Int,
        val peakVitality: Float,
        val voidVitality: Float,
        val status: String
    )

    /**
     * Updates the emergence simulation based on the latest student positions and behavioral events.
     *
     * This method calculates temporary "impulses" from recent behavior (last 24 hours) and then
     * applies cellular automata rules (diffusion and decay) to the global vitality grid.
     *
     * @param students List of current students with their logical (X, Y) positions.
     * @param events List of behavior events to process as impulses.
     * @param deltaTime Time step for the update (defaults to 1.0).
     * @return The updated vitality grid as a flat FloatArray of size [GRID_SIZE * GRID_SIZE].
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

    /**
     * Returns the current state of the vitality grid.
     */
    fun getGrid() = vitalityGrid

    /**
     * Analyzes the current vitality grid to calculate emergent patterns.
     * Ported from `Python/ghost_emergence_analysis.py`.
     *
     * @param vitalityGrid The grid to analyze.
     * @return An [EmergenceAnalysis] object containing peak values and dominant status.
     */
    fun analyzeEmergence(vitalityGrid: FloatArray): EmergenceAnalysis {
        val posCount = vitalityGrid.count { it > 0.1f }
        val negCount = vitalityGrid.count { it < -0.1f }
        val peakVitality = vitalityGrid.maxOrNull() ?: 0f
        val voidVitality = vitalityGrid.minOrNull() ?: 0f
        val status = if (posCount > negCount) "Growth Dominant" else "Decay Warning"

        return EmergenceAnalysis(posCount, negCount, peakVitality, voidVitality, status)
    }

    /**
     * Generates a high-fidelity Markdown report of the emergence simulation.
     * Parity-matched with `Python/ghost_emergence_analysis.py` for cross-platform auditing.
     *
     * @param analysis The analysis results to include in the report.
     * @return A formatted Markdown string.
     */
    fun generateEmergenceReport(analysis: EmergenceAnalysis): String {
        val report = StringBuilder()
        report.append("# Ghost Emergence Analysis Report\n")
        report.append("===============================\n")
        report.append("Emergent Clusters (Positive): ${analysis.posCount}\n")
        report.append("Emergent Voids (Negative): ${analysis.negCount}\n")
        report.append("Peak Vitality: ${String.format(java.util.Locale.US, "%.2f", analysis.peakVitality)}\n")
        report.append("Maximum Entropy/Void: ${String.format(java.util.Locale.US, "%.2f", analysis.voidVitality)}\n\n")
        report.append("Status: ${analysis.status}\n\n")
        report.append("---\n")
        report.append("*Generated by Ghost Emergence Engine v1.0 (Experimental)*")

        return report.toString()
    }
}
