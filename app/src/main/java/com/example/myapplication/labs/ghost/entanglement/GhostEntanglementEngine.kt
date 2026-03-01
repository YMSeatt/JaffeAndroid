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
     * Calculates the coherence between two students.
     * Ported from `Python/ghost_entanglement.py`.
     */
    fun calculateCoherence(
        nodeA: EntangledNode,
        nodeB: EntangledNode,
        sharingGroup: Boolean = false
    ): Float {
        // Physical Proximity factor (Gaussian)
        val dx = nodeA.x - nodeB.x
        val dy = nodeA.y - nodeB.y
        val dist = sqrt(dx * dx + dy * dy)
        val spatialCoherence = exp(-(dist * dist) / (2 * 600f * 600f))

        // Behavioral & Academic Synchronicity
        val syncFactor = (nodeA.behaviorSync + nodeB.behaviorSync) / 2f
        val parityFactor = 1f - abs(nodeA.academicParity - nodeB.academicParity)

        // Group multiplier: Being in the same group significantly boosts entanglement
        val groupMultiplier = if (sharingGroup) 1.5f else 1.0f

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
     * Helper to calculate student synchronicity metrics.
     */
    fun calculateNodeMetrics(
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): Pair<Float, Float> {
        // behaviorSync: Measures the "tempo" of behavior logs
        val bSync = if (behaviorLogs.isEmpty()) 0.5f else {
            val timestamps = behaviorLogs.map { it.timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }.sorted()
            if (timestamps.size < 2) 0.5f else {
                val intervals = mutableListOf<Long>()
                for (i in 0 until timestamps.size - 1) {
                    intervals.add(timestamps[i+1] - timestamps[i])
                }
                val avgInterval = intervals.average()
                // Higher sync for consistent intervals (lower variance)
                val variance = intervals.map { (it - avgInterval) * (it - avgInterval) }.average()
                exp(-variance / (1000f * 1000f * 60f)).toFloat().coerceIn(0f, 1f)
            }
        }

        // academicParity: Measures overall performance level
        val aParity = if (quizLogs.isEmpty() && homeworkLogs.isEmpty()) 0.5f else {
            val qAvg = if (quizLogs.isNotEmpty()) {
                quizLogs.mapNotNull { it.markValue?.let { v -> it.maxMarkValue?.let { m -> v / m } } }.average().toFloat()
            } else 0.5f
            val hAvg = if (homeworkLogs.isNotEmpty()) {
                homeworkLogs.count { it.status.contains("Done", ignoreCase = true) }.toFloat() / homeworkLogs.size
            } else 0.5f
            (qAvg + hAvg) / 2f
        }

        return Pair(bSync, aParity)
    }
}
