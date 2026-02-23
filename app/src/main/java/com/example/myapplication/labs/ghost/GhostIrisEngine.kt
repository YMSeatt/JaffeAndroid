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
     */
    fun calculateIris(
        studentId: Long,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): IrisParameters {
        // Deterministic seed based on student ID
        val seed = (studentId * 12345.678f) % 1000f

        // Calculate performance metrics
        val positiveCount = behaviorLogs.count { !it.type.contains("Negative", ignoreCase = true) }
        val negativeCount = behaviorLogs.count { it.type.contains("Negative", ignoreCase = true) }
        val behaviorBalance = if (behaviorLogs.isEmpty()) 0.5f
            else (positiveCount.toFloat() / behaviorLogs.size).coerceIn(0f, 1f)

        val avgQuiz = if (quizLogs.isNotEmpty()) {
            quizLogs.mapNotNull { it.markValue?.let { v -> it.maxMarkValue?.let { m -> if (m > 0) v / m else null } } }.average().toFloat()
        } else 0.75f

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
        val totalLogs = behaviorLogs.size + quizLogs.size + homeworkLogs.size
        val complexity = (totalLogs.toFloat() / 20f).coerceIn(0.1f, 1.0f)

        return IrisParameters(
            seed = seed,
            colorA = colorA,
            colorB = colorB,
            complexity = complexity
        )
    }
}
