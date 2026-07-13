package com.example.myapplication.labs.ghost.tectonics

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.sqrt

/**
 * GhostTectonicEngine: Models classroom social stability as a geological system.
 *
 * This engine calculates "Social Stress" and identifies "Fault Lines" based on
 * student proximity and behavioral history. High stress areas are prone to
 * "Seismic Events" (behavioral outbursts).
 *
 * ### Geological Metaphor:
 * - **Social Stress**: A cumulative metric where negative behavior (0.15f weight)
 *   increases localized pressure. Stress radiates and decays vertically and
 *   horizontally from each student.
 * - **Fault Lines**: Formed between high-stress clusters (>0.4f stress) in close
 *   proximity (<600 logical units).
 * - **Seismic Events**: Predicted behavioral outbursts in critical zones.
 */
object GhostTectonicEngine {

    /**
     * Represents a student's position and their calculated social stress.
     *
     * @property id The student's database ID.
     * @property x The horizontal logical coordinate.
     * @property y The vertical logical coordinate.
     * @property stress The calculated social stress level, normalized from 0.0 (Stable) to 1.0 (Critical).
     */
    data class TectonicNode(
        val id: Long,
        val x: Float,
        val y: Float,
        val stress: Float
    )

    /**
     * Encapsulates the macroscopic social stability metrics of the classroom.
     *
     * @property avgStress The mean stress level across all tracked students.
     * @property peakStress The maximum stress level identified in any single student.
     * @property faultLineCount The number of active "Fault Lines" (high-stress interactions).
     * @property riskLevel The overall categorical [RiskLevel] of the classroom.
     */
    data class SeismicAnalysis(
        val avgStress: Float,
        val peakStress: Float,
        val faultLineCount: Int,
        val riskLevel: RiskLevel
    )

    /**
     * Categorizes the classroom's social stability risk.
     */
    enum class RiskLevel {
        /** Nominal social pressure; no significant fault lines. */
        STABLE,
        /** Stress is building; monitor student proximity. */
        ACCUMULATING,
        /** High social friction; tremors (outbursts) likely in specific clusters. */
        VOLATILE,
        /** URGENT: Social fault lines are at breaking point; event imminent. */
        CRITICAL
    }

    /**
     * The radius (in logical units) within which a student's stress influences their neighbors.
     * Calibrated for the 4000x4000 logical canvas.
     */
    private const val STRESS_RADIUS = 600f

    /**
     * The impact of a single negative behavioral log on a student's base stress level.
     */
    private const val NEGATIVE_LOG_WEIGHT = 0.15f

    /**
     * Calculates the tectonic state of the classroom.
     * Ported to Python for parity in `ghost_tectonics_analysis.py`.
     *
     * BOLT: Optimized overload that accepts raw Student entities and pre-calculated
     * negative counts. Replaced functional operators with manual loops to eliminate
     * intermediate allocations (filter, groupBy, associate, map).
     */
    fun calculateTectonicState(
        students: List<com.example.myapplication.data.Student>,
        negativeCounts: Map<Long, Int>
    ): List<TectonicNode> {
        val n = students.size
        if (n == 0) return emptyList()

        val baseStresses = FloatArray(n)
        for (i in 0 until n) {
            val logCount = negativeCounts[students[i].id] ?: 0
            baseStresses[i] = (logCount * NEGATIVE_LOG_WEIGHT).coerceAtMost(0.5f)
        }

        val nodes = ArrayList<TectonicNode>(n)
        val stressRadiusSq = STRESS_RADIUS * STRESS_RADIUS

        for (i in 0 until n) {
            val s1 = students[i]
            var proximityStress = 0f

            for (j in 0 until n) {
                if (i == j) continue
                val s2 = students[j]

                val dx = s1.xPosition - s2.xPosition
                val dy = s1.yPosition - s2.yPosition
                val distSq = dx * dx + dy * dy

                if (distSq < stressRadiusSq) {
                    val dist = sqrt(distSq)
                    // Stress radiates and decays with distance
                    proximityStress += (baseStresses[j] * (1f - dist / STRESS_RADIUS)) * 0.5f
                }
            }

            val totalStress = (baseStresses[i] + proximityStress).coerceIn(0f, 1f)
            nodes.add(TectonicNode(s1.id, s1.xPosition, s1.yPosition, totalStress))
        }
        return nodes
    }

    /**
     * BOLT: Compatibility overload for tests and legacy callers.
     */
    fun calculateTectonicState(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>
    ): List<TectonicNode> {
        val negativeCounts = mutableMapOf<Long, Int>()
        for (log in behaviorLogs) {
            if (log.type.contains("Negative", ignoreCase = true)) {
                negativeCounts[log.studentId] = (negativeCounts[log.studentId] ?: 0) + 1
            }
        }

        val rawStudents = students.map { ui ->
            com.example.myapplication.data.Student(
                id = ui.id.toLong(),
                firstName = "", lastName = "", gender = "",
                xPosition = ui.xPosition.value,
                yPosition = ui.yPosition.value
            )
        }
        return calculateTectonicState(rawStudents, negativeCounts)
    }

    /**
     * Performs a macroscopic seismic analysis of the classroom.
     *
     * BOLT: Optimized to use squared distance comparisons to avoid expensive sqrt calls.
     */
    fun analyzeSeismicRisk(nodes: List<TectonicNode>): SeismicAnalysis {
        if (nodes.isEmpty()) return SeismicAnalysis(0f, 0f, 0, RiskLevel.STABLE)

        var totalStress = 0f
        var peakStress = 0f
        var faultLines = 0
        val stressRadiusSq = STRESS_RADIUS * STRESS_RADIUS

        for (i in nodes.indices) {
            val n1 = nodes[i]
            totalStress += n1.stress
            if (n1.stress > peakStress) peakStress = n1.stress

            for (j in i + 1 until nodes.size) {
                val n2 = nodes[j]
                val dx = n1.x - n2.x
                val dy = n1.y - n2.y
                val distSq = dx * dx + dy * dy

                // A "Fault Line" exists between two high-stress nodes in close proximity
                if (distSq < stressRadiusSq && n1.stress > 0.4f && n2.stress > 0.4f) {
                    faultLines++
                }
            }
        }

        val avgStress = totalStress / nodes.size
        val riskLevel = when {
            peakStress > 0.8f || faultLines > 3 -> RiskLevel.CRITICAL
            peakStress > 0.6f || faultLines > 1 -> RiskLevel.VOLATILE
            avgStress > 0.3f -> RiskLevel.ACCUMULATING
            else -> RiskLevel.STABLE
        }

        return SeismicAnalysis(avgStress, peakStress, faultLines, riskLevel)
    }

    /**
     * Generates a "Seismic Activity Report" in Markdown.
     */
    fun generateSeismicReport(analysis: SeismicAnalysis): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val report = StringBuilder()

        report.append("# \uD83C\uDF0B GHOST TECTONICS: SEISMIC ACTIVITY REPORT\n")
        report.append("**Classroom Risk Level:** ${analysis.riskLevel.name}\n")
        report.append("**Average Social Stress:** ${String.format(Locale.US, "%.1f", analysis.avgStress * 100f)}%\n")
        report.append("**Peak Stress Node:** ${String.format(Locale.US, "%.1f", analysis.peakStress * 100f)}%\n")
        report.append("**Active Fault Lines:** ${analysis.faultLineCount}\n")
        report.append("**Timestamp:** $timestamp\n\n")

        report.append("---\n\n")

        report.append("## [STRUCTURAL INTERPRETATION]\n")
        when (analysis.riskLevel) {
            RiskLevel.CRITICAL -> report.append("URGENT: Major seismic event imminent. Social fault lines are at breaking point. Structural reorganization recommended.\n")
            RiskLevel.VOLATILE -> report.append("WARNING: High social friction detected. Significant tremors expected in high-stress clusters.\n")
            RiskLevel.ACCUMULATING -> report.append("NOTICE: Stress is building in the social lithosphere. Monitor cluster proximity.\n")
            RiskLevel.STABLE -> report.append("NOMINAL: The classroom's social structure is stable. No significant stress detected.\n")
        }

        report.append("\n---\n*Generated by Ghost Tectonic Engine v1.0 (Experimental)*")

        return report.toString()
    }
}
