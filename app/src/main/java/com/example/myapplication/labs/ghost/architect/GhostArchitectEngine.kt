package com.example.myapplication.labs.ghost.architect

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import com.example.myapplication.ui.model.StudentUiItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

/**
 * GhostArchitectEngine: A strategic generative engine for classroom layout optimization.
 *
 * Unlike the [GhostCognitiveEngine] which focuses on physics-based distribution,
 * Ghost Architect uses pedagogical heuristics to propose "Neural Trajectories"
 * for student seating.
 *
 * ### Logic Parity & Heuristics
 * This engine is a mobile-optimized port of the algorithm defined in `Python/ghost_architect_analysis.py`.
 * Several implementation adjustments have been made for the real-time Android environment:
 *
 * 1. **Proximity Threshold**: Android uses a distance threshold of **2000/3000 logical units**,
 *    which is 2x larger than the Python prototype's 1000/1500. This compensates for the
 *    4000x4000 logical canvas scale used on Android vs. Python's smaller workspace.
 */
object GhostArchitectEngine {

    enum class StrategicGoal {
        COLLABORATION, // Minimize distance between social allies
        FOCUS,         // Maximize distance between friction points
        STABILITY      // Balance classroom by diffusing high-energy nodes
    }

    data class ProposedMove(
        val studentId: Long,
        val currentX: Float,
        val currentY: Float,
        val proposedX: Float,
        val proposedY: Float,
        val weight: Float // 0.0 to 1.0 (Importance of the move)
    )

    private val REPORT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Calculates a global synergy score (0.0 to 1.0) based on how well
     * the current layout matches a pedagogical goal.
     * Ported from Python blueprint (ghost_architect_analysis.py).
     *
     * ### Heuristics:
     * - **COLLABORATION**: Measures closeness of allies. Synergy peaks (1.0) when all
     *   collaborators are within 2000 units (scaled from Python's 1000).
     * - **FOCUS**: Measures separation of friction points. Synergy peaks (1.0) when
     *   friction points are at least 3000 units apart (scaled from Python's 1500).
     *
     * BOLT: Optimized to use manual loops instead of `filter` to avoid list allocations.
     */
    fun calculateSynergy(
        students: List<StudentUiItem>,
        edges: List<GhostLatticeEngine.Edge>,
        goal: StrategicGoal
    ): Float {
        if (students.isEmpty()) return 0.0f
        val studentMap = students.associateBy { it.id.toLong() }

        return when (goal) {
            StrategicGoal.COLLABORATION -> {
                var totalDist = 0.0
                var count = 0
                edges.forEach { edge ->
                    if (edge.type == GhostLatticeEngine.ConnectionType.COLLABORATION) {
                        val s1 = studentMap[edge.fromId]
                        val s2 = studentMap[edge.toId]
                        if (s1 != null && s2 != null) {
                            val dx = s1.xPosition.value - s2.xPosition.value
                            val dy = s1.yPosition.value - s2.yPosition.value
                            val dist = sqrt(dx * dx + dy * dy)
                            totalDist += min(dist.toDouble(), 2000.0) // 2x scaled from Python's 1000
                            count++
                        }
                    }
                }
                if (count == 0) return 1.0f
                val avgDist = totalDist / count
                (1.0 - (avgDist / 2000.0)).toFloat().coerceIn(0.0f, 1.0f)
            }
            StrategicGoal.FOCUS -> {
                var totalDist = 0.0
                var count = 0
                edges.forEach { edge ->
                    if (edge.type == GhostLatticeEngine.ConnectionType.FRICTION) {
                        val s1 = studentMap[edge.fromId]
                        val s2 = studentMap[edge.toId]
                        if (s1 != null && s2 != null) {
                            val dx = s1.xPosition.value - s2.xPosition.value
                            val dy = s1.yPosition.value - s2.yPosition.value
                            val dist = sqrt(dx * dx + dy * dy)
                            totalDist += min(dist.toDouble(), 3000.0) // 2x scaled from Python's 1500
                            count++
                        }
                    }
                }
                if (count == 0) return 1.0f
                val avgDist = totalDist / count
                (avgDist / 3000.0).toFloat().coerceIn(0.0f, 1.0f)
            }
            /**
             * STABILITY synergy logic is currently a placeholder (1.0f).
             * Future iterations will incorporate variance-based metrics for student
             * distribution relative to behavioral hot-spots.
             */
            StrategicGoal.STABILITY -> 1.0f
        }
    }

    /**
     * Generates a Markdown report summary of the layout's neural alignment.
     * Ported from Python blueprint (ghost_architect_analysis.py).
     */
    fun generateReport(
        students: List<StudentUiItem>,
        edges: List<GhostLatticeEngine.Edge>
    ): String {
        val timestamp = LocalDateTime.now().format(REPORT_FORMATTER)
        val collabScore = calculateSynergy(students, edges, StrategicGoal.COLLABORATION)
        val focusScore = calculateSynergy(students, edges, StrategicGoal.FOCUS)

        return """
# 🏗️ GHOST ARCHITECT: STRATEGIC LAYOUT REPORT
**Generated:** $timestamp
**Classroom Sample:** ${students.size} students

## 🛰️ Neural Alignment Metrics
- **Collaboration Synergy:** ${"%.1f".format(Locale.US, collabScore * 100)}%
- **Focus Reliability:** ${"%.1f".format(Locale.US, focusScore * 100)}%

---
*Generated by Ghost Architect Android Engine v1.0 (Experimental)*
        """.trimIndent()
    }

    /**
     * Proposes a set of moves for the classroom based on the selected strategic goal.
     *
     * This function analyzes the current student positions against social lattice edges
     * and historical behavior logs to generate "Neural Trajectories".
     *
     * ### Move Heuristics:
     * - **COLLABORATION**: Pulls students 30% towards the centroid of their social allies.
     * - **FOCUS**: Pushes students 200 units away from friction points.
     * - **STABILITY**: Pulls high-negative-count students 20% towards the classroom center (2000, 2000).
     *
     * BOLT: Uses O(N + E) analysis by pre-mapping lattice edges into [edgesByStudent]
     * and pre-calculating [negativeCounts], avoiding $O(N \times E)$ or $O(N \times L)$ bottlenecks.
     */
    fun proposeLayout(
        students: List<StudentUiItem>,
        edges: List<GhostLatticeEngine.Edge>,
        behaviorLogs: List<BehaviorEvent>,
        goal: StrategicGoal
    ): List<ProposedMove> {
        val moves = mutableListOf<ProposedMove>()
        val studentMap = students.associateBy { it.id.toLong() }

        // Group edges by student to transform O(N * E) -> O(N + E)
        val edgesByStudent = mutableMapOf<Long, MutableList<GhostLatticeEngine.Edge>>()
        edges.forEach { edge ->
            edgesByStudent.getOrPut(edge.fromId) { mutableListOf() }.add(edge)
            edgesByStudent.getOrPut(edge.toId) { mutableListOf() }.add(edge)
        }

        // BOLT: Pre-calculate negative counts to transform STABILITY O(N * E) -> O(N + E)
        val negativeCounts = if (goal == StrategicGoal.STABILITY) {
            val counts = mutableMapOf<Long, Int>()
            behaviorLogs.forEach { log ->
                if (log.type.contains("Negative", ignoreCase = true)) {
                    counts[log.studentId] = (counts[log.studentId] ?: 0) + 1
                }
            }
            counts
        } else emptyMap()

        students.forEach { student ->
            val sId = student.id.toLong()
            val sEdges = edgesByStudent[sId] ?: emptyList()

            var targetX = student.xPosition.value
            var targetY = student.yPosition.value
            var moveWeight = 0f

            when (goal) {
                StrategicGoal.COLLABORATION -> {
                    // BOLT: Manual loop instead of filter to avoid list allocations
                    var collabCount = 0
                    var sumX = 0f
                    var sumY = 0f
                    sEdges.forEach { edge ->
                        if (edge.type == GhostLatticeEngine.ConnectionType.COLLABORATION) {
                            val otherId = if (edge.fromId == sId) edge.toId else edge.fromId
                            studentMap[otherId]?.let {
                                sumX += it.xPosition.value
                                sumY += it.yPosition.value
                                collabCount++
                            }
                        }
                    }

                    if (collabCount > 0) {
                        // Propose moving 30% towards the centroid of collaborators
                        targetX = student.xPosition.value + (sumX / collabCount - student.xPosition.value) * 0.3f
                        targetY = student.yPosition.value + (sumY / collabCount - student.yPosition.value) * 0.3f
                        moveWeight = 0.5f + (collabCount * 0.1f).coerceAtMost(0.5f)
                    }
                }
                StrategicGoal.FOCUS -> {
                    // BOLT: Manual loop instead of filter
                    var frictionCount = 0
                    var pushX = 0f
                    var pushY = 0f
                    sEdges.forEach { edge ->
                        if (edge.type == GhostLatticeEngine.ConnectionType.FRICTION) {
                            val otherId = if (edge.fromId == sId) edge.toId else edge.fromId
                            studentMap[otherId]?.let {
                                val dx = student.xPosition.value - it.xPosition.value
                                val dy = student.yPosition.value - it.yPosition.value
                                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                                pushX += (dx / dist) * 200f // Push 200 units away
                                pushY += (dy / dist) * 200f
                                frictionCount++
                            }
                        }
                    }

                    if (frictionCount > 0) {
                        targetX = (student.xPosition.value + pushX).coerceIn(100f, 3900f)
                        targetY = (student.yPosition.value + pushY).coerceIn(100f, 3900f)
                        moveWeight = 0.8f // High priority for focus-based moves
                    }
                }
                StrategicGoal.STABILITY -> {
                    // Diffuse high-negative-log students towards the center or empty zones
                    val negativeCount = negativeCounts[sId] ?: 0
                    if (negativeCount > 2) {
                        // High energy node: move towards classroom center (2000, 2000)
                        targetX = student.xPosition.value + (2000f - student.xPosition.value) * 0.2f
                        targetY = student.yPosition.value + (2000f - student.yPosition.value) * 0.2f
                        moveWeight = (negativeCount * 0.15f).coerceAtMost(1.0f)
                    }
                }
            }

            // BOLT: Use squared distance comparison to avoid sqrt and pow(2) calls
            val dx = targetX - student.xPosition.value
            val dy = targetY - student.yPosition.value
            val distSq = dx * dx + dy * dy
            if (distSq > 100f) { // 100f = 10f * 10f
                moves.add(
                    ProposedMove(
                        studentId = sId,
                        currentX = student.xPosition.value,
                        currentY = student.yPosition.value,
                        proposedX = targetX,
                        proposedY = targetY,
                        weight = moveWeight
                    )
                )
            }
        }

        return moves
    }
}
