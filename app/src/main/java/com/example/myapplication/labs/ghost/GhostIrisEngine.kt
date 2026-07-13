package com.example.myapplication.labs.ghost

import androidx.compose.ui.graphics.Color
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import kotlin.math.abs

/**
 * GhostIrisEngine: Computes visual parameters for the Neural Iris based on student data.
 */
object GhostIrisEngine {

    data class IrisParameters(
        val seed: Float,
        val colorA: Color,
        val colorB: Color,
        val complexity: Float
    )

    /**
     * Generates iris parameters for a student.
     *
     * BOLT: Refactored to accept pre-calculated metrics to eliminate redundant O(L)
     * log traversals in the background update pipeline.
     *
     * @param studentId The unique student ID.
     * @param behaviorBalance Normalized behavior stability (0.0 to 1.0).
     * @param avgQuiz Normalized average quiz score (0.0 to 1.0).
     * @param totalLogs Combined count of behavior, quiz, and homework logs.
     */
    fun calculateIris(
        studentId: Long,
        behaviorBalance: Float,
        avgQuiz: Float,
        totalLogs: Int
    ): IrisParameters {
        // Deterministic seed based on student ID
        val seed = (studentId * 12345.678f) % 1000f

        // Colors based on behavior balance
        // Positive: Cyan/Blue, Negative: Red/Purple, Neutral: Green/Teal
        val colorA = when {
            behaviorBalance > 0.7f -> Color(0xFF00FFFF) // Cyan
            behaviorBalance < 0.3f -> Color(0xFFFF00FF) // Magenta
            else -> Color(0xFF00FF88) // Spring Green
        }

        val colorB = when {
            avgQuiz > 0.8f -> Color(0xFFFFFFFF) // White (Peak)
            avgQuiz < 0.5f -> Color(0xFF550000) // Deep Red (Trouble)
            else -> Color(0xFF004488) // Deep Blue
        }

        // Complexity based on log density (more logs = more complex iris)
        val complexity = (totalLogs.toFloat() / 20f).coerceIn(0.1f, 1.0f)

        return IrisParameters(
            seed = seed,
            colorA = colorA,
            colorB = colorB,
            complexity = complexity
        )
    }
}
