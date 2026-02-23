package com.example.myapplication.labs.ghost.vector

import com.example.myapplication.labs.ghost.lattice.GhostLatticeEngine
import java.util.Locale
import kotlin.math.*

/**
 * GhostVectorEngine: Calculates the "Social Gravity" vectors for students.
 *
 * This engine sums the attraction and repulsion forces from social lattice edges
 * to determine the net direction and magnitude of social influence on each student.
 * It provides a mathematical model of classroom social dynamics.
 *
 * This implementation is parity-matched with `Python/ghost_vector_analysis.py`.
 */
class GhostVectorEngine {

    /**
     * Classifies the social status of a student based on their net force magnitude.
     */
    enum class SocialStatus {
        NOMINAL,
        HIGH_TURBULENCE,
        ISOLATED,
        ACTIVE_SYNERGY
    }

    /**
     * Represents the calculated social force vector for a specific student.
     *
     * @property studentId The ID of the student the vector applies to.
     * @property dx The horizontal component of the net force.
     * @property dy The vertical component of the net force.
     * @property magnitude The total intensity of the social force.
     * @property angle The direction of the force in radians (atan2).
     * @property status The social categorization based on magnitude.
     */
    data class SocialVector(
        val studentId: Long,
        val dx: Float,
        val dy: Float,
        val magnitude: Float,
        val angle: Float,
        val status: SocialStatus
    )

    /**
     * Represents the global social cohesion analysis for the classroom.
     */
    data class SocialAnalysis(
        val cohesionIndex: Float,
        val globalStatus: String
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
        val nodeMap = nodes.associateBy { it.id }

        // Group edges by student ID to transform O(S * E) into O(S + E)
        val edgesByStudent = mutableMapOf<Long, MutableList<GhostLatticeEngine.Edge>>()
        edges.forEach { edge ->
            edgesByStudent.getOrPut(edge.fromId) { mutableListOf() }.add(edge)
            edgesByStudent.getOrPut(edge.toId) { mutableListOf() }.add(edge)
        }

        return nodes.map { node ->
            var netDx = 0f
            var netDy = 0f

            val studentEdges = edgesByStudent[node.id] ?: emptyList()
            studentEdges.forEach { edge ->
                val otherId = if (edge.fromId == node.id) edge.toId else edge.fromId
                val otherNode = nodeMap[otherId] ?: return@forEach

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

            val mag = sqrt(netDx * netDx + netDy * netDy)

            // Social Status classification matching Python thresholds
            val status = when {
                mag > 85f -> SocialStatus.HIGH_TURBULENCE
                mag < 5f -> SocialStatus.ISOLATED
                mag > 40f -> SocialStatus.ACTIVE_SYNERGY
                else -> SocialStatus.NOMINAL
            }

            SocialVector(
                studentId = node.id,
                dx = netDx,
                dy = netDy,
                magnitude = mag,
                angle = atan2(netDy, netDx),
                status = status
            )
        }
    }

    /**
     * Analyzes the overall classroom cohesion based on calculated vectors.
     *
     * @param vectors The list of individual student social vectors.
     * @return A [SocialAnalysis] containing global metrics.
     */
    fun analyzeClassroomCohesion(vectors: List<SocialVector>): SocialAnalysis {
        val avgCohesion = if (vectors.isNotEmpty()) {
            vectors.sumOf { it.magnitude.toDouble() }.toFloat() / vectors.size
        } else 0f

        val status = if (avgCohesion < 50f) "STABLE" else "DYNAMIC"

        return SocialAnalysis(avgCohesion, status)
    }

    /**
     * Generates a Markdown-formatted social cohesion report.
     * Parity-matched with `Python/ghost_vector_analysis.py`.
     *
     * @param analysis The global classroom analysis.
     * @param vectors The list of student social vectors.
     * @param studentNames A map of student IDs to their display names.
     * @return A formatted Markdown string.
     */
    fun generateSocialReport(
        analysis: SocialAnalysis,
        vectors: List<SocialVector>,
        studentNames: Map<Long, String>
    ): String {
        val timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )

        val report = StringBuilder()
        report.append("# 👻 GHOST VECTOR: SOCIAL COHESION ANALYSIS\n")
        report.append("**Classroom Cohesion Index:** ${String.format(Locale.US, "%.2f", analysis.cohesionIndex)}\n")
        report.append("**Global Status:** ${analysis.globalStatus}\n")
        report.append("**Timestamp:** $timestamp\n\n")
        report.append("---\n\n")
        report.append("## 🛰️ Neural Trajectory & Turbulence\n")
        report.append("Each student's 'Net Force' represents their current social momentum within the classroom grid.\n\n")
        report.append("| Student | Net Force (mG) | Social Status |\n")
        report.append("| :--- | :--- | :--- |\n")

        vectors.sortedByDescending { it.magnitude }.forEach { vector ->
            val name = studentNames[vector.studentId] ?: "Student ${vector.studentId}"
            val statusStr = vector.status.name.replace("_", " ")
            report.append("| $name | ${String.format(Locale.US, "%.2f", vector.magnitude)} | $statusStr |\n")
        }

        report.append("\n---\n*Generated by Ghost Vector Analysis Bridge v1.0 (Experimental)*")
        return report.toString()
    }
}
