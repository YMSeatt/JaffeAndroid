package com.example.myapplication.labs.ghost.helix

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog

/**
 * GhostHelixEngine: Calculates "Neural DNA" sequences and trajectory markers.
 *
 * This engine transforms raw classroom logs into a "Genetic Sequence" that drives
 * the Ghost Helix visualization. Each student has a unique DNA signature derived
 * from their behavioral and academic history.
 *
 * ### Genetic Mapping:
 * - **Adenine (A)**: Positive behavior events.
 * - **Thymine (T)**: Negative behavior events.
 * - **Cytosine (C)**: High academic performance (>60%).
 * - **Guanine (G)**: Academic turbulence or performance drops.
 */
object GhostHelixEngine {

    /**
     * The constituent building blocks of a student's Neural DNA.
     */
    enum class BasePairType {
        ADENINE,  // Positive Behavior
        THYMINE,  // Negative Behavior
        CYTOSINE, // High Academic Performance
        GUANINE   // Academic Turbulence
    }

    /**
     * A single data point in the genetic sequence.
     * @property type The base pair classification (A, T, C, G).
     * @property intensity The "strength" of the event (0.0 to 1.0).
     * @property timestamp The time the event occurred, used for chronological sequencing.
     */
    data class NeuralBasePair(
        val type: BasePairType,
        val intensity: Float, // 0..1
        val timestamp: Long
    )

    /**
     * The complete sequenced DNA of a student, containing visual parameters for the shader.
     * @property studentId The unique identifier of the student.
     * @property basePairs The chronological list of base pairs.
     * @property twistRate Driving rotation speed for the AGSL helix (1.0 to 3.0).
     * @property stability Normalized stability score (0.1 to 1.0) driving visual jitter.
     */
    data class HelixSequence(
        val studentId: Long,
        val basePairs: List<NeuralBasePair>,
        val twistRate: Float,  // Overall rotation speed
        val stability: Float   // How "stable" the helix appears (low jitter)
    )

    /**
     * Sequences a student's behavioral and academic logs into a Neural DNA helix.
     *
     * @param studentId The ID of the student to sequence.
     * @param behaviorLogs Current set of behavior events.
     * @param quizLogs Current set of academic quiz logs.
     * @return A [HelixSequence] containing the base pairs and calculated visual parameters.
     */
    fun sequenceStudentData(
        studentId: Long,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>
    ): HelixSequence {
        val basePairs = mutableListOf<NeuralBasePair>()

        // 1. Process Behavior Logs
        behaviorLogs.forEach { log ->
            val isNegative = log.type.contains("Negative", ignoreCase = true)
            basePairs.add(
                NeuralBasePair(
                    type = if (isNegative) BasePairType.THYMINE else BasePairType.ADENINE,
                    intensity = 0.8f,
                    timestamp = log.timestamp
                )
            )
        }

        // 2. Process Academic Logs (Quiz)
        quizLogs.forEach { log ->
            val ratio = if (log.maxMarkValue != null && log.maxMarkValue > 0) {
                (log.markValue ?: 0.0) / log.maxMarkValue
            } else 0.7

            val isLow = ratio < 0.6
            basePairs.add(
                NeuralBasePair(
                    type = if (isLow) BasePairType.GUANINE else BasePairType.CYTOSINE,
                    intensity = ratio.toFloat().coerceIn(0.1f, 1.0f),
                    timestamp = log.loggedAt
                )
            )
        }

        // Sort by timestamp to maintain sequence order
        basePairs.sortBy { it.timestamp }

        // Calculate Helix Global Parameters
        val negativeCount = basePairs.count { it.type == BasePairType.THYMINE || it.type == BasePairType.GUANINE }
        val totalCount = basePairs.size.coerceAtLeast(1)
        val stability = (1.0f - (negativeCount.toFloat() / totalCount)).coerceIn(0.1f, 1.0f)

        // Twist rate increases with academic activity
        val academicActivity = basePairs.count { it.type == BasePairType.CYTOSINE || it.type == BasePairType.GUANINE }
        val twistRate = 1.0f + (academicActivity.toFloat() / 10.0f).coerceIn(0f, 2f)

        return HelixSequence(
            studentId = studentId,
            basePairs = basePairs,
            twistRate = twistRate,
            stability = stability
        )
    }

    /**
     * Analyzes the "Genetic Trajectory" of a sequence to determine social/academic momentum.
     *
     * This logic is ported from `Python/ghost_helix_analysis.py` to maintain cross-platform
     * parity. It uses a weighted scoring system where Adenine and Cytosine contribute
     * positively, while Thymine and Guanine contribute negatively.
     *
     * @param sequence The sequenced Neural DNA.
     * @return A normalized trajectory score (0.0 to 1.0).
     */
    fun calculateTrajectory(sequence: HelixSequence): Float {
        if (sequence.basePairs.isEmpty()) return 0.5f

        var score = 0f
        sequence.basePairs.forEach { bp ->
            val weight = when (bp.type) {
                BasePairType.ADENINE -> 1.0f
                BasePairType.CYTOSINE -> 0.8f
                BasePairType.THYMINE -> -1.0f
                BasePairType.GUANINE -> -0.6f
            }
            score += weight * bp.intensity
        }

        // Normalize to 0..1 range
        val normalized = 0.5f + (score / sequence.basePairs.size.coerceAtLeast(1)) * 0.5f
        return normalized.coerceIn(0f, 1f)
    }
}
