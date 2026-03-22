package com.example.myapplication.labs.ghost.architect

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.*

/**
 * GhostArchitectEngine: A strategic generative engine for classroom layout optimization.
 *
 * Unlike the [GhostCognitiveEngine] which focuses on physics-based distribution,
 * Ghost Architect uses pedagogical heuristics to propose "Neural Trajectories"
 * for student seating.
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

    /**
     * Proposes a set of moves for the classroom based on the selected strategic goal.
     *
     * BOLT: Uses O(N + E) analysis by pre-mapping lattice edges.
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

        students.forEach { student ->
            val sId = student.id.toLong()
            val sEdges = edgesByStudent[sId] ?: emptyList()

            var targetX = student.xPosition.value
            var targetY = student.yPosition.value
            var moveWeight = 0f

            when (goal) {
                StrategicGoal.COLLABORATION -> {
                    // Pull closer to collaboration partners
                    val partners = sEdges.filter { it.type == GhostLatticeEngine.ConnectionType.COLLABORATION }
                    if (partners.isNotEmpty()) {
                        var sumX = 0f
                        var sumY = 0f
                        partners.forEach { edge ->
                            val otherId = if (edge.fromId == sId) edge.toId else edge.fromId
                            studentMap[otherId]?.let {
                                sumX += it.xPosition.value
                                sumY += it.yPosition.value
                            }
                        }
                        // Propose moving 30% towards the centroid of collaborators
                        targetX = student.xPosition.value + (sumX / partners.size - student.xPosition.value) * 0.3f
                        targetY = student.yPosition.value + (sumY / partners.size - student.yPosition.value) * 0.3f
                        moveWeight = 0.5f + (partners.size * 0.1f).coerceAtMost(0.5f)
                    }
                }
                StrategicGoal.FOCUS -> {
                    // Push away from friction points
                    val friction = sEdges.filter { it.type == GhostLatticeEngine.ConnectionType.FRICTION }
                    if (friction.isNotEmpty()) {
                        var pushX = 0f
                        var pushY = 0f
                        friction.forEach { edge ->
                            val otherId = if (edge.fromId == sId) edge.toId else edge.fromId
                            studentMap[otherId]?.let {
                                val dx = student.xPosition.value - it.xPosition.value
                                val dy = student.yPosition.value - it.yPosition.value
                                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                                pushX += (dx / dist) * 200f // Push 200 units away
                                pushY += (dy / dist) * 200f
                            }
                        }
                        targetX = (student.xPosition.value + pushX).coerceIn(100f, 3900f)
                        targetY = (student.yPosition.value + pushY).coerceIn(100f, 3900f)
                        moveWeight = 0.8f // High priority for focus-based moves
                    }
                }
                StrategicGoal.STABILITY -> {
                    // Diffuse high-negative-log students towards the center or empty zones
                    val negativeCount = behaviorLogs.count { it.studentId == sId && it.type.contains("Negative", ignoreCase = true) }
                    if (negativeCount > 2) {
                        // High energy node: move towards classroom center (2000, 2000)
                        targetX = student.xPosition.value + (2000f - student.xPosition.value) * 0.2f
                        targetY = student.yPosition.value + (2000f - student.yPosition.value) * 0.2f
                        moveWeight = (negativeCount * 0.15f).coerceAtMost(1.0f)
                    }
                }
            }

            // Only propose move if it's significant (> 10 logical units)
            val dist = sqrt((targetX - student.xPosition.value).pow(2) + (targetY - student.yPosition.value).pow(2))
            if (dist > 10f) {
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
