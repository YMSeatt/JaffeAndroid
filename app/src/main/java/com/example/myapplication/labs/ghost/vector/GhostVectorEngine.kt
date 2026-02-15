package com.example.myapplication.labs.ghost.vector

import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import kotlin.math.*

/**
 * GhostVectorEngine: Calculates the "Social Gravity" vectors for students.
 *
 * This engine sums the attraction and repulsion forces from social lattice edges
 * to determine the net direction and magnitude of social influence on each student.
 * It provides a mathematical model of classroom social dynamics.
 */
class GhostVectorEngine {

    /**
     * Represents the calculated social force vector for a specific student.
     *
     * @property studentId The ID of the student the vector applies to.
     * @property dx The horizontal component of the net force.
     * @property dy The vertical component of the net force.
     * @property magnitude The total intensity of the social force.
     * @property angle The direction of the force in radians (atan2).
     */
    data class SocialVector(
        val studentId: Long,
        val dx: Float,
        val dy: Float,
        val magnitude: Float,
        val angle: Float
    )

    /**
     * Calculates the social vectors for all students given the current lattice nodes and edges.
     *
     * @param nodes Spatial representations of students.
     * @param edges Inferred social connections between students.
     * @return A list of [SocialVector]s, one for each student node.
     */
    fun calculateVectors(
        nodes: List<GhostLatticeEngine.LatticeNode>,
        edges: List<GhostLatticeEngine.Edge>
    ): List<SocialVector> {
        return nodes.map { node ->
            var netDx = 0f
            var netDy = 0f

            edges.forEach { edge ->
                if (edge.fromId == node.id || edge.toId == node.id) {
                    val otherId = if (edge.fromId == node.id) edge.toId else edge.fromId
                    val otherNode = nodes.find { it.id == otherId } ?: return@forEach

                    val dx = otherNode.x - node.x
                    val dy = otherNode.y - node.y
                    val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

                    // Normalize direction
                    val dirX = dx / dist
                    val dirY = dy / dist

                    // Force calculation based on connection type
                    // COLLABORATION: Positive force (Attracts)
                    // FRICTION: Negative force (Repels)
                    // NEUTRAL: Mild positive force
                    val forceMagnitude = when (edge.type) {
                        GhostLatticeEngine.ConnectionType.COLLABORATION -> edge.strength * 60f
                        GhostLatticeEngine.ConnectionType.FRICTION -> -edge.strength * 100f
                        GhostLatticeEngine.ConnectionType.NEUTRAL -> edge.strength * 15f
                    }

                    netDx += dirX * forceMagnitude
                    netDy += dirY * forceMagnitude
                }
            }

            val mag = sqrt(netDx * netDx + netDy * netDy)
            SocialVector(
                studentId = node.id,
                dx = netDx,
                dy = netDy,
                magnitude = mag,
                angle = atan2(netDy, netDx)
            )
        }
    }
}
