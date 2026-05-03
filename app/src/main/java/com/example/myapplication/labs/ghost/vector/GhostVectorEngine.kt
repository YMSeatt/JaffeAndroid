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
     * BOLT ⚡ Optimization:
     * 1. **Zero-Allocation Force Loop**: Processes edges in a single pass using the Action-Reaction
     *    principle (Newton's Third Law). For each edge between A and B, it calculates the vector
     *    once and applies it to both A and B (with reversed sign), cutting trig/math by 50%.
     * 2. **Eliminated Intermediate Collections**: Removed `HashMap` and `MutableList` grouping
     *    logic from the hot path.
     * 3. **Primitive Accumulation**: Uses `FloatArray`s for net force components to avoid boxing.
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
        if (n == 0) return emptyList()
        val eSize = edges.size

        val netDx = FloatArray(n)
        val netDy = FloatArray(n)

        // Pre-map IDs to indices for O(1) lookup during edge processing
        val idToIndex = mutableMapOf<Long, Int>()
        for (i in 0 until n) {
            idToIndex[nodes[i].id] = i
        }

        // Action-Reaction pass: Process each edge once
        for (i in 0 until eSize) {
            val edge = edges[i]
            val idxA = idToIndex[edge.fromId] ?: continue
            val idxB = idToIndex[edge.toId] ?: continue

            val nodeA = nodes[idxA]
            val nodeB = nodes[idxB]

            val dx = nodeB.x - nodeA.x
            val dy = nodeB.y - nodeA.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(MIN_DISTANCE_SAFETY)

            val forceMagnitude = when (edge.type) {
                GhostLatticeEngine.ConnectionType.COLLABORATION -> edge.strength * FORCE_COLLABORATION
                GhostLatticeEngine.ConnectionType.FRICTION -> edge.strength * FORCE_FRICTION
                GhostLatticeEngine.ConnectionType.NEUTRAL -> edge.strength * FORCE_NEUTRAL
            }

            val fx = (dx / dist) * forceMagnitude
            val fy = (dy / dist) * forceMagnitude

            // Apply force to A (towards B if positive)
            netDx[idxA] += fx
            netDy[idxA] += fy

            // Apply opposite force to B (towards A if positive)
            netDx[idxB] -= fx
            netDy[idxB] -= fy
        }

        val result = ArrayList<SocialVector>(n)
        for (i in 0 until n) {
            val dx = netDx[i]
            val dy = netDy[i]
            val mag = sqrt(dx * dx + dy * dy)

            val status = when {
                mag > THRESHOLD_TURBULENCE -> SocialStatus.HIGH_TURBULENCE
                mag < THRESHOLD_ISOLATED -> SocialStatus.ISOLATED
                mag > THRESHOLD_SYNERGY -> SocialStatus.ACTIVE_SYNERGY
                else -> SocialStatus.NOMINAL
            }

            result.add(SocialVector(
                studentId = nodes[i].id,
                dx = dx,
                dy = dy,
                magnitude = mag,
                angle = atan2(dy, dx),
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
        if (n == 0) return emptyList()
        val eSize = edges.size

        val netDx = FloatArray(n)
        val netDy = FloatArray(n)

        val idToIndex = mutableMapOf<Long, Int>()
        for (i in 0 until n) {
            idToIndex[students[i].id] = i
        }

        for (i in 0 until eSize) {
            val edge = edges[i]
            val idxA = idToIndex[edge.fromId] ?: continue
            val idxB = idToIndex[edge.toId] ?: continue

            val sA = students[idxA]
            val sB = students[idxB]

            val dx = sB.xPosition - sA.xPosition
            val dy = sB.yPosition - sA.yPosition
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(MIN_DISTANCE_SAFETY)

            val forceMagnitude = when (edge.type) {
                GhostLatticeEngine.ConnectionType.COLLABORATION -> edge.strength * FORCE_COLLABORATION
                GhostLatticeEngine.ConnectionType.FRICTION -> edge.strength * FORCE_FRICTION
                GhostLatticeEngine.ConnectionType.NEUTRAL -> edge.strength * FORCE_NEUTRAL
            }

            val fx = (dx / dist) * forceMagnitude
            val fy = (dy / dist) * forceMagnitude

            netDx[idxA] += fx
            netDy[idxA] += fy

            netDx[idxB] -= fx
            netDy[idxB] -= fy
        }

        val result = ArrayList<SocialVector>(n)
        for (i in 0 until n) {
            val dx = netDx[i]
            val dy = netDy[i]
            val mag = sqrt(dx * dx + dy * dy)

            val status = when {
                mag > THRESHOLD_TURBULENCE -> SocialStatus.HIGH_TURBULENCE
                mag < THRESHOLD_ISOLATED -> SocialStatus.ISOLATED
                mag > THRESHOLD_SYNERGY -> SocialStatus.ACTIVE_SYNERGY
                else -> SocialStatus.NOMINAL
            }

            result.add(SocialVector(
                studentId = students[i].id,
                dx = dx,
                dy = dy,
                magnitude = mag,
                angle = atan2(dy, dx),
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
