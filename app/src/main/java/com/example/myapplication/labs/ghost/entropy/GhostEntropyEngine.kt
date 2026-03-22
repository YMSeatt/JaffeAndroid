package com.example.myapplication.labs.ghost.entropy

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * GhostEntropyEngine: Calculates "Neural Entropy" and Behavioral Uncertainty.
 *
 * This engine models students as "high-entropy" nodes based on the predictability of
 * their behavioral events and academic performance variance. Higher entropy implies
 * a "turbulent" or "chaotic" student state.
 *
 * ### Thermodynamic Metaphor:
 * - **Behavioral Entropy**: Shannon entropy applied to the distribution of behavior
 *   types. Higher diversity of behaviors (positive, negative, etc.) increases
 *   unpredictability.
 * - **Academic Variance**: Normalized statistical variance in quiz scores.
 * - **Entropy Score**: Weighted combination (60% Behavioral, 40% Academic) used
 *    to drive AGSL "Thermal Distortion" effects.
 */
object GhostEntropyEngine {

    data class EntropyNode(
        val id: Long,
        val x: Float,
        val y: Float,
        val behaviorEntropy: Float, // 0..1 (Shannon entropy of behavior types)
        val academicVariance: Float // 0..1 (Normalized variance in quiz scores)
    )

    data class EntropyAnalysis(
        val globalEntropy: Float,
        val highEntropyCount: Int,
        val criticalNodes: List<Long>
    )

    /**
     * Calculates the Shannon Entropy of behavior types for a student.
     * Ported to Python/ghost_entropy_analyzer.py.
     *
     * BOLT: Optimized to O(L) single-pass manual loop to avoid functional operator overhead.
     */
    fun calculateBehaviorEntropy(logs: List<BehaviorEvent>): Float {
        if (logs.isEmpty()) return 0f

        val counts = mutableMapOf<String, Int>()
        for (log in logs) {
            val type = log.type
            counts[type] = (counts[type] ?: 0) + 1
        }
        val total = logs.size.toFloat()

        var entropy = 0f
        for (count in counts.values) {
            val p = count / total
            entropy -= (p * ln(p))
        }

        // Normalize: Max entropy for N types is ln(N). We'll cap at ln(5) for normalization.
        val maxPossible = ln(5f)
        return (entropy / maxPossible).coerceIn(0f, 1f)
    }

    /**
     * Calculates the normalized variance of academic performance.
     *
     * BOLT: Optimized to O(N) single-pass without intermediate list allocations.
     */
    fun calculateAcademicVariance(quizLogs: List<QuizLog>): Float {
        if (quizLogs.size < 2) return 0f

        var sum = 0.0
        var sumSq = 0.0
        var count = 0

        for (log in quizLogs) {
            val v = log.markValue
            val m = log.maxMarkValue
            if (v != null && m != null && m > 0) {
                val score = v / m
                sum += score
                sumSq += score * score
                count++
            }
        }

        if (count < 2) return 0f

        val mean = sum / count
        val variance = (sumSq / count) - (mean * mean)

        // Max variance for values in 0..1 is 0.25. Normalize to 0..1.
        return (variance.toFloat() / 0.25f).coerceIn(0f, 1f)
    }

    /**
     * Combines behavior entropy and academic variance into a single "Entropy Score".
     */
    fun calculateEntropyScore(bEntropy: Float, aVariance: Float): Float {
        return (bEntropy * 0.6f + aVariance * 0.4f).coerceIn(0f, 1f)
    }

    /**
     * Analyzes the overall classroom entropy.
     */
    fun analyzeClassroomEntropy(nodes: List<EntropyNode>): EntropyAnalysis {
        if (nodes.isEmpty()) return EntropyAnalysis(0f, 0, emptyList())

        val scores = nodes.map { calculateEntropyScore(it.behaviorEntropy, it.academicVariance) }
        val avgEntropy = scores.average().toFloat()

        val highEntropyThreshold = 0.7f
        val criticalNodes = nodes.filter { calculateEntropyScore(it.behaviorEntropy, it.academicVariance) > highEntropyThreshold }
            .map { it.id }

        return EntropyAnalysis(avgEntropy, criticalNodes.size, criticalNodes)
    }

    /**
     * Generates a Markdown report of the classroom's neural entropy.
     */
    fun generateEntropyReport(
        analysis: EntropyAnalysis,
        timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    ): String {
        val report = StringBuilder()
        report.append("# 👻 GHOST ENTROPY: NEURAL TURBULENCE ANALYSIS\n")
        report.append("**Global Entropy Index:** ${String.format(Locale.US, "%.1f", analysis.globalEntropy * 100f)}%\n")
        report.append("**High-Entropy Nodes:** ${analysis.highEntropyCount}\n")
        report.append("**Timestamp:** $timestamp\n\n")

        report.append("---\n\n")

        report.append("## [INTERPRETATION]\n")
        when {
            analysis.globalEntropy > 0.8f -> report.append("CRITICAL: Extreme neural turbulence detected. Classroom stability is compromised.\n")
            analysis.globalEntropy > 0.5f -> report.append("WARNING: Moderate entropy levels. Expect unpredictable behavioral fluctuations.\n")
            else -> report.append("STABLE: Neural entropy within nominal parameters. Behavior is predictable.\n")
        }

        if (analysis.criticalNodes.isNotEmpty()) {
            report.append("\n**Critical Focus Nodes:** ${analysis.criticalNodes.size} students exhibit entropy above the 70% threshold.\n")
        }

        report.append("\n---\n*Generated by Ghost Entropy Engine v1.0 (Experimental)*")

        return report.toString()
    }
}
