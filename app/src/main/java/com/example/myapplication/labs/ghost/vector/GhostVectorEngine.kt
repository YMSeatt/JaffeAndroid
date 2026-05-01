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
object GhostVectorEngine {

    /** Force multiplier for collaborative (attraction) interactions. */
    private const val FORCE_COLLABORATION = 60f
    /** Force multiplier for friction-based (repulsion) interactions. */
    private const val FORCE_FRICTION = -100f
    /** Force multiplier for neutral, proximity-based interactions. */
    private const val FORCE_NEUTRAL = 15f

    /** Net force threshold (mG) for classifying a student as "High Turbulence". */
    private const val THRESHOLD_TURBULENCE = 85f
    /** Net force threshold (mG) for classifying a student as "Active Synergy". */
    private const val THRESHOLD_SYNERGY = 40f
    /** Net force threshold (mG) below which a student is considered "Isolated". */
    private const val THRESHOLD_ISOLATED = 5f

    /** Minimum distance safety to avoid division by zero during normalization. */
    private const val MIN_DISTANCE_SAFETY = 1f

    /** The global cohesion index threshold for a "Dynamic" classroom state. */
    private const val COHESION_DYNAMIC_THRESHOLD = 50f

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
     * BOLT: Optimized with manual index-based loops to minimize iterator overhead.
     *
     * @param nodes Spatial representations of students.
     * @param edges Inferred social connections between students.
     * @return A list of [SocialVector]s, one for each student node.
     */
    fun calculateVectors(
        nodes: List<GhostLatticeEngine.LatticeNode>,
        edges: List<GhostLatticeEngine.Edge>
    ): List<SocialVector> {
        val n = nodes.size
        val eSize = edges.size

        // Group edges by student ID to transform O(S * E) into O(S + E)
        val edgesByStudent = mutableMapOf<Long, MutableList<GhostLatticeEngine.Edge>>()
        for (i in 0 until eSize) {
            val edge = edges[i]
            edgesByStudent.getOrPut(edge.fromId) { mutableListOf() }.add(edge)
            edgesByStudent.getOrPut(edge.toId) { mutableListOf() }.add(edge)
        }

        val nodeMap = mutableMapOf<Long, GhostLatticeEngine.LatticeNode>()
        for (i in 0 until n) {
            val node = nodes[i]
            nodeMap[node.id] = node
        }

        val result = ArrayList<SocialVector>(n)
        for (i in 0 until n) {
            val node = nodes[i]
            var netDx = 0f
            var netDy = 0f

            val studentEdges = edgesByStudent[node.id]
            if (studentEdges != null) {
                val sEdgesSize = studentEdges.size
                for (j in 0 until sEdgesSize) {
                    val edge = studentEdges[j]
                    val otherId = if (edge.fromId == node.id) edge.toId else edge.fromId
                    val otherNode = nodeMap[otherId] ?: continue

                    val dx = otherNode.x - node.x
                    val dy = otherNode.y - node.y
                    val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(MIN_DISTANCE_SAFETY)

                    // Normalize direction
                    val dirX = dx / dist
                    val dirY = dy / dist

                    // Force calculation based on connection type
                    val forceMagnitude = when (edge.type) {
                        GhostLatticeEngine.ConnectionType.COLLABORATION -> edge.strength * FORCE_COLLABORATION
                        GhostLatticeEngine.ConnectionType.FRICTION -> edge.strength * FORCE_FRICTION
                        GhostLatticeEngine.ConnectionType.NEUTRAL -> edge.strength * FORCE_NEUTRAL
                    }

                    netDx += dirX * forceMagnitude
                    netDy += dirY * forceMagnitude
                }
            }

            val mag = sqrt(netDx * netDx + netDy * netDy)

            // Social Status classification matching Python thresholds
            val status = when {
                mag > THRESHOLD_TURBULENCE -> SocialStatus.HIGH_TURBULENCE
                mag < THRESHOLD_ISOLATED -> SocialStatus.ISOLATED
                mag > THRESHOLD_SYNERGY -> SocialStatus.ACTIVE_SYNERGY
                else -> SocialStatus.NOMINAL
            }

            result.add(SocialVector(
                studentId = node.id,
                dx = netDx,
                dy = netDy,
                magnitude = mag,
                angle = atan2(netDy, netDx),
                status = status
            ))
        }
        return result
    }

    /**
     * BOLT: Optimized overload for Student entities to avoid intermediate LatticeNode allocations.
     *
     * @param students The list of student entities with positions.
     * @param edges Inferred social connections between students.
     * @return A list of [SocialVector]s.
     */
    fun calculateVectorsForStudents(
        students: List<com.example.myapplication.data.Student>,
        edges: List<GhostLatticeEngine.Edge>
    ): List<SocialVector> {
        val n = students.size
        val eSize = edges.size

        val edgesByStudent = mutableMapOf<Long, MutableList<GhostLatticeEngine.Edge>>()
        for (i in 0 until eSize) {
            val edge = edges[i]
            edgesByStudent.getOrPut(edge.fromId) { mutableListOf() }.add(edge)
            edgesByStudent.getOrPut(edge.toId) { mutableListOf() }.add(edge)
        }

        val studentMap = mutableMapOf<Long, com.example.myapplication.data.Student>()
        for (i in 0 until n) {
            val s = students[i]
            studentMap[s.id] = s
        }

        val result = ArrayList<SocialVector>(n)
        for (i in 0 until n) {
            val student = students[i]
            var netDx = 0f
            var netDy = 0f

            val studentEdges = edgesByStudent[student.id]
            if (studentEdges != null) {
                val sEdgesSize = studentEdges.size
                for (j in 0 until sEdgesSize) {
                    val edge = studentEdges[j]
                    val otherId = if (edge.fromId == student.id) edge.toId else edge.fromId
                    val otherStudent = studentMap[otherId] ?: continue

                    val dx = otherStudent.xPosition - student.xPosition
                    val dy = otherStudent.yPosition - student.yPosition
                    val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(MIN_DISTANCE_SAFETY)

                    val dirX = dx / dist
                    val dirY = dy / dist

                    val forceMagnitude = when (edge.type) {
                        GhostLatticeEngine.ConnectionType.COLLABORATION -> edge.strength * FORCE_COLLABORATION
                        GhostLatticeEngine.ConnectionType.FRICTION -> edge.strength * FORCE_FRICTION
                        GhostLatticeEngine.ConnectionType.NEUTRAL -> edge.strength * FORCE_NEUTRAL
                    }

                    netDx += dirX * forceMagnitude
                    netDy += dirY * forceMagnitude
                }
            }

            val mag = sqrt(netDx * netDx + netDy * netDy)
            val status = when {
                mag > THRESHOLD_TURBULENCE -> SocialStatus.HIGH_TURBULENCE
                mag < THRESHOLD_ISOLATED -> SocialStatus.ISOLATED
                mag > THRESHOLD_SYNERGY -> SocialStatus.ACTIVE_SYNERGY
                else -> SocialStatus.NOMINAL
            }

            result.add(SocialVector(
                studentId = student.id,
                dx = netDx,
                dy = netDy,
                magnitude = mag,
                angle = atan2(netDy, netDx),
                status = status
            ))
        }
        return result
    }

    /**
     * Analyzes the overall classroom cohesion based on calculated vectors.
     *
     * @param vectors The list of individual student social vectors.
     * @return A [SocialAnalysis] containing global metrics.
     */
    fun analyzeClassroomCohesion(vectors: List<SocialVector>): SocialAnalysis {
        val size = vectors.size
        var totalMag = 0.0
        for (i in 0 until size) {
            totalMag += vectors[i].magnitude.toDouble()
        }
        val avgCohesion = if (size > 0) totalMag.toFloat() / size else 0f

        val status = if (avgCohesion < COHESION_DYNAMIC_THRESHOLD) "STABLE" else "DYNAMIC"

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

        val sortedVectors = vectors.sortedByDescending { it.magnitude }
        val vSize = sortedVectors.size
        for (i in 0 until vSize) {
            val vector = sortedVectors[i]
            val name = studentNames[vector.studentId] ?: "Student ${vector.studentId}"
            val statusStr = vector.status.name.replace("_", " ")
            report.append("| $name | ${String.format(Locale.US, "%.2f", vector.magnitude)} | $statusStr |\n")
        }

        report.append("\n---\n*Generated by Ghost Vector Analysis Bridge v1.0 (Experimental)*")
        return report.toString()
    }
}
