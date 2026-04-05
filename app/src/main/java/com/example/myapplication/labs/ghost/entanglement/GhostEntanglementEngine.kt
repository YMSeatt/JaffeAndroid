package com.example.myapplication.labs.ghost.entanglement

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * GhostEntanglementEngine: Calculates "Quantum Coherence" and Synchronicity.
 *
 * This engine models students as "entangled" nodes. Coherence is increased when
 * students share groups, exhibit similar behavioral timing, or have high academic
 * parity. It represents a "spooky action at a distance" in social dynamics.
 */
object GhostEntanglementEngine {

    data class EntangledNode(
        val id: Long,
        val x: Float,
        val y: Float,
        val behaviorSync: Float, // 0..1 (Synchronicity in log timing)
        val academicParity: Float // 0..1 (Similarity in performance)
    )

    data class EntanglementLink(
        val studentA: Long,
        val studentB: Long,
        val coherence: Float // 0..1 (Strength of the entanglement)
    )

    enum class CoherenceState {
        DECOHERED,
        STABLE,
        SUPERPOSITION,
        ENTANGLED
    }

    data class EntanglementAnalysis(
        val state: CoherenceState,
        val avgCoherence: Float,
        val maxCoherence: Float,
        val activeLinks: Int
    )

    /**
     * Calculates the Coherence Score (0.0 to 1.0) between two students.
     * Ported from the Python R&D script `ghost_entanglement.py`.
     *
     * The score represents the strength of social "entanglement" based on three pillars:
     * 1. **Spatial (40%)**: Gaussian decay of physical distance. Sigma is set to 600 logical units.
     * 2. **Behavioral (30%)**: Average of student behavioral synchronicity metrics.
     * 3. **Academic (30%)**: Parity of academic performance (1.0 - absolute difference).
     *
     * @param nodeA The spatial and behavioral representation of the first student.
     * @param nodeB The spatial and behavioral representation of the second student.
     * @param sharingGroup Whether the students are in the same classroom group (applies a 1.5x boost).
     * @return A normalized coherence factor.
     */
    fun calculateCoherence(
        nodeA: EntangledNode,
        nodeB: EntangledNode,
        sharingGroup: Boolean = false
    ): Float {
        // 1. Spatial Pillar: Gaussian Proximity
        // Models the "Social Field" overlap. sigma = 600 units.
        val dx = nodeA.x - nodeB.x
        val dy = nodeA.y - nodeB.y
        val dist = sqrt(dx * dx + dy * dy)
        val spatialCoherence = exp(-(dist * dist) / (2 * 600f * 600f))

        // 2. Behavioral Pillar: Average Sync
        val syncFactor = (nodeA.behaviorSync + nodeB.behaviorSync) / 2f

        // 3. Academic Pillar: Performance Parity
        val parityFactor = 1f - abs(nodeA.academicParity - nodeB.academicParity)

        // Catalyst: Shared group membership provides a significant "Quantum Boost" (1.5x)
        val groupMultiplier = if (sharingGroup) 1.5f else 1.0f

        // Weighted Summation
        return ((spatialCoherence * 0.4f + syncFactor * 0.3f + parityFactor * 0.3f) * groupMultiplier).coerceIn(0f, 1f)
    }

    /**
     * Analyzes the overall classroom entanglement.
     */
    fun analyzeEntanglement(
        nodes: List<EntangledNode>,
        groupMap: Map<Long, Long?> // studentId -> groupId
    ): EntanglementAnalysis {
        if (nodes.size < 2) return EntanglementAnalysis(CoherenceState.DECOHERED, 0f, 0f, 0)

        var totalCoherence = 0f
        var maxCoherence = 0f
        var linkCount = 0

        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val nodeA = nodes[i]
                val nodeB = nodes[j]
                val sameGroup = groupMap[nodeA.id] != null && groupMap[nodeA.id] == groupMap[nodeB.id]

                val coherence = calculateCoherence(nodeA, nodeB, sameGroup)
                if (coherence > 0.6f) {
                    totalCoherence += coherence
                    if (coherence > maxCoherence) maxCoherence = coherence
                    linkCount++
                }
            }
        }

        val avgCoherence = if (linkCount > 0) totalCoherence / linkCount else 0f

        val state = when {
            avgCoherence > 0.8f -> CoherenceState.ENTANGLED
            avgCoherence > 0.5f -> CoherenceState.SUPERPOSITION
            avgCoherence > 0.2f -> CoherenceState.STABLE
            else -> CoherenceState.DECOHERED
        }

        return EntanglementAnalysis(state, avgCoherence, maxCoherence, linkCount)
    }

    /**
     * Generates a Markdown report of the classroom's quantum entanglement.
     */
    fun generateEntanglementReport(
        analysis: EntanglementAnalysis,
        timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    ): String {
        val report = StringBuilder()
        report.append("# 👻 GHOST ENTANGLEMENT: QUANTUM SOCIAL ANALYSIS\n")
        report.append("**Classroom Coherence State:** ${analysis.state.name}\n")
        report.append("**Avg Coherence Score:** ${String.format(Locale.US, "%.1f", analysis.avgCoherence * 100f)}%\n")
        report.append("**Active Quantum Links:** ${analysis.activeLinks}\n")
        report.append("**Timestamp:** $timestamp\n\n")

        report.append("---\n\n")

        report.append("## [INTERPRETATION]\n")
        when (analysis.state) {
            CoherenceState.ENTANGLED -> report.append("Maximum synchronicity achieved. The classroom is operating as a single unified quantum system.\n")
            CoherenceState.SUPERPOSITION -> report.append("High probability of social contagion. Behavioral events in one node will rapidly propagate to entangled partners.\n")
            CoherenceState.STABLE -> report.append("Coherence is nominal. Social interactions are predictable and localized.\n")
            CoherenceState.DECOHERED -> report.append("Low social synchronicity detected. Students are operating as isolated observers.\n")
        }

        report.append("\n---\n*Generated by Ghost Entanglement Engine v1.0 (Experimental)*")

        return report.toString()
    }

    /**
     * Identifies the strongest quantum links in the classroom.
     *
     * BOLT: Optimized to identify the top [limit] entangled pairs based on real coherence.
     */
    fun identifyEntangledLinks(
        nodes: List<EntangledNode>,
        groupMap: Map<Long, Long?>,
        limit: Int = 3
    ): List<EntanglementLink> {
        if (nodes.size < 2) return emptyList()

        val links = mutableListOf<EntanglementLink>()

        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val nodeA = nodes[i]
                val nodeB = nodes[j]
                val sameGroup = groupMap[nodeA.id] != null && groupMap[nodeA.id] == groupMap[nodeB.id]

                val coherence = calculateCoherence(nodeA, nodeB, sameGroup)
                if (coherence > 0.7f) { // Threshold for visual entanglement
                    links.add(EntanglementLink(nodeA.id, nodeB.id, coherence))
                }
            }
        }

        return links.sortedByDescending { it.coherence }.take(limit)
    }

    /**
     * Calculates high-fidelity synchronicity metrics for a single student node.
     *
     * This method processes historical logs to extract a student's "Social Signature":
     * - **Behavior Sync**: A measure of "Temporal Tempo" (0.0 to 1.0). High scores indicate
     *   regular, predictable log intervals, suggesting synchronized interaction.
     * - **Academic Parity Base**: A measure of overall performance (0.0 to 1.0).
     *
     * **BOLT Optimization**: Replaced functional operators with manual loops to minimize
     * object allocations and list traversals during neural analysis.
     */
    fun calculateNodeMetrics(
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): Pair<Float, Float> {
        // 1. behaviorSync: Measures the "Tempo Variance" of behavior logs.
        val bSync = if (behaviorLogs.isEmpty()) 0.5f else {
            val n = behaviorLogs.size
            val timestamps = LongArray(n)
            for (i in 0 until n) {
                timestamps[i] = behaviorLogs[i].timestamp
            }
            timestamps.sort()

            if (n < 2) 0.5f else {
                var totalInterval = 0.0
                val intervals = DoubleArray(n - 1)
                for (i in 0 until n - 1) {
                    val interval = (timestamps[i + 1] - timestamps[i]).toDouble()
                    intervals[i] = interval
                    totalInterval += interval
                }
                val avgInterval = totalInterval / (n - 1)

                // Higher sync for consistent intervals (lower variance)
                var totalVariance = 0.0
                for (interval in intervals) {
                    val diff = interval - avgInterval
                    totalVariance += diff * diff
                }
                val variance = totalVariance / (n - 1)
                exp(-variance / (1000.0 * 1000.0 * 60.0)).toFloat().coerceIn(0f, 1f)
            }
        }

        // academicParity: Measures overall performance level
        val aParity = if (quizLogs.isEmpty() && homeworkLogs.isEmpty()) 0.5f else {
            val qAvg = if (quizLogs.isEmpty()) 0.5f else {
                var totalRatio = 0.0
                var count = 0
                for (log in quizLogs) {
                    val v = log.markValue
                    val m = log.maxMarkValue
                    if (v != null && m != null && m > 0) {
                        totalRatio += (v / m)
                        count++
                    }
                }
                if (count > 0) (totalRatio / count).toFloat() else 0.5f
            }

            val hAvg = if (homeworkLogs.isEmpty()) 0.5f else {
                var doneCount = 0
                for (log in homeworkLogs) {
                    if (log.status.contains("Done", ignoreCase = true)) {
                        doneCount++
                    }
                }
                doneCount.toFloat() / homeworkLogs.size
            }
            (qAvg + hAvg) / 2f
        }

        return Pair(bSync, aParity)
    }
}
