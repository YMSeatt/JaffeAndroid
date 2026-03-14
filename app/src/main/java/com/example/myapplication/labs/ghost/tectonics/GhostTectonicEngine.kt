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
 */
object GhostTectonicEngine {

    data class TectonicNode(
        val id: Long,
        val x: Float,
        val y: Float,
        val stress: Float // 0.0 (Stable) to 1.0 (Critical)
    )

    data class SeismicAnalysis(
        val avgStress: Float,
        val peakStress: Float,
        val faultLineCount: Int,
        val riskLevel: RiskLevel
    )

    enum class RiskLevel {
        STABLE,
        ACCUMULATING,
        VOLATILE,
        CRITICAL
    }

    private const val STRESS_RADIUS = 600f
    private const val NEGATIVE_LOG_WEIGHT = 0.15f

    /**
     * Calculates the tectonic state of the classroom.
     * Ported to Python for parity in `ghost_tectonics_analysis.py`.
     */
    fun calculateTectonicState(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>
    ): List<TectonicNode> {
        val negativeLogsByStudent = behaviorLogs
            .filter { it.type.contains("Negative", ignoreCase = true) }
            .groupBy { it.studentId }

        // 1. Calculate base stress per student from their own logs
        val baseStressMap = students.associate { student ->
            val logCount = negativeLogsByStudent[student.id.toLong()]?.size ?: 0
            student.id.toLong() to (logCount * NEGATIVE_LOG_WEIGHT).coerceAtMost(0.5f)
        }

        // 2. Calculate social pressure from proximity to other high-stress nodes
        return students.map { s1 ->
            var proximityStress = 0f
            val s1Id = s1.id.toLong()

            students.forEach { s2 ->
                val s2Id = s2.id.toLong()
                if (s1Id != s2Id) {
                    val dx = s1.xPosition.value - s2.xPosition.value
                    val dy = s1.yPosition.value - s2.yPosition.value
                    val dist = sqrt(dx * dx + dy * dy)

                    if (dist < STRESS_RADIUS) {
                        val s2BaseStress = baseStressMap[s2Id] ?: 0f
                        // Stress radiates and decays with distance
                        proximityStress += (s2BaseStress * (1f - dist / STRESS_RADIUS)) * 0.5f
                    }
                }
            }

            val totalStress = ((baseStressMap[s1Id] ?: 0f) + proximityStress).coerceIn(0f, 1f)
            TectonicNode(s1Id, s1.xPosition.value, s1.yPosition.value, totalStress)
        }
    }

    /**
     * Performs a macroscopic seismic analysis of the classroom.
     */
    fun analyzeSeismicRisk(nodes: List<TectonicNode>): SeismicAnalysis {
        if (nodes.isEmpty()) return SeismicAnalysis(0f, 0f, 0, RiskLevel.STABLE)

        var totalStress = 0f
        var peakStress = 0f
        var faultLines = 0

        for (i in nodes.indices) {
            val n1 = nodes[i]
            totalStress += n1.stress
            if (n1.stress > peakStress) peakStress = n1.stress

            for (j in i + 1 until nodes.size) {
                val n2 = nodes[j]
                val dx = n1.x - n2.x
                val dy = n1.y - n2.y
                val dist = sqrt(dx * dx + dy * dy)

                // A "Fault Line" exists between two high-stress nodes in close proximity
                if (dist < STRESS_RADIUS && n1.stress > 0.4f && n2.stress > 0.4f) {
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
