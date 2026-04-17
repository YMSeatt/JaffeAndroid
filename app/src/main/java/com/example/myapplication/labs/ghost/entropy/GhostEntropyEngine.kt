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
     * This measures the "unpredictability" of a student's behavior. A student who
     * consistently exhibits only one type of behavior has 0 entropy, while a student
     * with a perfectly even distribution of many different behaviors has high entropy.
     *
     * BOLT: Refactored to accept pre-calculated counts to eliminate redundant log
     * traversals in the background update pipeline, achieving O(1) performance
     * relative to the number of logs.
     *
     * @param typeCounts Map of behavior type names to their occurrence counts.
     * @param totalLogs Total number of behavioral logs for the student.
     */
    fun calculateBehaviorEntropy(typeCounts: Map<String, Int>, totalLogs: Int): Float {
        if (totalLogs == 0) return 0f

        val total = totalLogs.toFloat()
        var entropy = 0f
        for (count in typeCounts.values) {
            val p = count / total
            if (p > 0) {
                entropy -= (p * ln(p))
            }
        }

        // Normalize: Max entropy for N types is ln(N). We'll cap at ln(5) for normalization.
        val maxPossible = ln(5f)
        return (entropy / maxPossible).coerceIn(0f, 1f)
    }

    /**
     * Calculates the normalized variance of academic performance.
     *
     * Variance is a measure of how spread out the student's quiz scores are.
     * High variance suggests inconsistent academic performance (e.g., alternating
     * between 0% and 100%).
     *
     * BOLT: Refactored to accept pre-calculated statistical moments (sum and sum of squares)
     * to allow O(1) calculation during the background update pipeline.
     *
     * @param sum Sum of quiz score ratios (markValue / maxMarkValue).
     * @param sumSq Sum of squares of quiz score ratios.
     * @param count Number of valid quiz logs included in the sums.
     */
    fun calculateAcademicVariance(sum: Double, sumSq: Double, count: Int): Float {
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
