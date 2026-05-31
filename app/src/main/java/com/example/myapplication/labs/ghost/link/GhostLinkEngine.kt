package com.example.myapplication.labs.ghost.link

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp
import kotlin.random.Random

/**
 * GhostLinkEngine: The futuristic "Neural Dossier" generator for Ghost Lab.
 *
 * This engine is a mobile-optimized port of `Python/ghost_link.py`. It simulates a
 * 2027-era AI analysis, transforming simple student metadata into a high-fidelity
 * report featuring predictive trajectories and complex neural metrics.
 *
 * Enhanced with "Neural Pairing" logic to identify high-synergy student connections
 * based on spatial proximity and behavioral alignment.
 */
object GhostLinkEngine {

    /** BOLT: Distance threshold for neural pairing on the 4000x4000 canvas. */
    private const val LINK_DISTANCE_THRESHOLD = 600f
    private const val LINK_DISTANCE_THRESHOLD_SQ = LINK_DISTANCE_THRESHOLD * LINK_DISTANCE_THRESHOLD

    data class StudentNode(
        val id: Long,
        val x: Float,
        val y: Float,
        val behavioralBalance: Float // 0..1, where 1.0 is perfectly positive
    )

    data class NeuralLink(
        val studentA: Long,
        val studentB: Long,
        val strength: Float // 0..1
    )

    /**
     * Identifies neural pairings between students based on proximity and behavioral synergy.
     *
     * BOLT: O(N^2) complexity but optimized with primitive math and early exits.
     */
    fun calculateLinks(nodes: List<StudentNode>): List<NeuralLink> {
        if (nodes.size < 2) return emptyList()

        val links = mutableListOf<NeuralLink>()

        for (i in nodes.indices) {
            val nodeA = nodes[i]
            for (j in i + 1 until nodes.size) {
                val nodeB = nodes[j]

                val dx = nodeA.x - nodeB.x
                val dy = nodeA.y - nodeB.y
                val distSq = dx * dx + dy * dy

                if (distSq < LINK_DISTANCE_THRESHOLD_SQ * 4) { // Scan up to 2x threshold
                    val proximity = exp(-distSq / (2 * LINK_DISTANCE_THRESHOLD_SQ))

                    // Synergy: Students with similar behavioral balance have higher synergy
                    val synergy = 1f - abs(nodeA.behavioralBalance - nodeB.behavioralBalance)

                    val strength = (proximity * 0.7f + synergy * 0.3f)

                    if (strength > 0.65f) {
                        links.add(NeuralLink(nodeA.id, nodeB.id, strength))
                    }
                }
            }
        }

        // BOLT: Limit to top 10 strongest links to avoid visual clutter
        return links.sortedByDescending { it.strength }.take(10)
    }

    /**
     * Generates a "Neural Dossier" report for a student.
     *
     * @param studentId The unique identifier of the student.
     * @param studentName The display name of the student.
     * @return A formatted Markdown string containing the Neural Dossier.
     */
    fun generateNeuralDossier(studentId: Long, studentName: String): String {
        val random = Random(studentId) // Seed with studentId for deterministic "AI" results per student

        val resonance = random.nextInt(70, 96)
        val flux = random.nextDouble(0.5, 0.9)
        val entropy = random.nextDouble(0.1, 0.4)
        val gravity = random.nextInt(10, 51)

        val timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
        )

        val trajectory = listOf(
            "Phase 1: Cognitive Baseline Established.",
            "Phase 2: Identifying Neural Hotspots in Social Interaction.",
            "Phase 3: Predicting Academic Synergy with neighboring nodes."
        )

        // PRIVACY: Mask the student name in the report content (e.g., "John Doe" -> "J. DOE")
        val nameParts = studentName.trim().split(" ")
        val maskedName = if (nameParts.size >= 2) {
            "${nameParts.first().take(1)}. ${nameParts.last()}"
        } else {
            studentName
        }.uppercase(Locale.US)

        val report = StringBuilder()
        report.append("# \uD83D\uDC7B GHOST NEURAL DOSSIER: $maskedName\n")
        report.append("**Status:** Teleported via Ghost Portal\n")
        // PRIVACY: Removed Internal ID to prevent leaking database state
        report.append("**Analysis Timestamp:** $timestamp\n\n")
        report.append("---\n\n")

        report.append("## \uD83E\uDDE0 Neural Metrics\n")
        report.append("| Metric | Value | Status |\n")
        report.append("| :--- | :--- | :--- |\n")
        report.append("| Cognitive Resonance | $resonance% | NOMINAL |\n")
        report.append("| Collaborative Flux | ${String.format(Locale.US, "%.2f", flux)} | NOMINAL |\n")
        report.append("| Academic Entropy | ${String.format(Locale.US, "%.2f", entropy)} | NOMINAL |\n")
        report.append("| Social Gravity | $gravity mG | NOMINAL |\n\n")

        report.append("## \uD83D\uDD2E Predictive Trajectory\n")
        trajectory.forEach { step ->
            report.append("- $step\n")
        }
        report.append("\n")

        report.append("## \uD83C\uDF00 Portal Log\n")
        report.append("Student data received from Android instance.\n")
        report.append("Packet Integrity: 100%\n")
        report.append("Neural Alignment: POSITIVE\n\n")

        report.append("---\n")
        report.append("*Generated by Ghost Link v1.0 (R&D Concept 2027)*")

        return report.toString()
    }
}
