package com.example.myapplication.labs.ghost.quasar

import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.data.BehaviorEvent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * GhostQuasarEngine: Identifies "Quasar Students" — high-energy nodes in the classroom ecosystem.
 *
 * A student becomes a Quasar if they meet specific criteria for behavioral log density
 * or rapid academic shifts, creating a visual "Gravity Well" and "Accretion Disk"
 * on the seating chart.
 */
object GhostQuasarEngine {

    data class QuasarState(
        val studentId: Long,
        val x: Float,
        val y: Float,
        val energy: Float, // 0.0 to 1.0
        val luminosity: Float,
        val behaviorPolarity: Float // -1.0 (Negative) to 1.0 (Positive)
    )

    /**
     * Identifies Quasars from a list of students and their behavioral logs.
     * Logic:
     * 1. Count logs in the last 30 minutes.
     * 2. Calculate "Energy" based on log frequency.
     * 3. Calculate "Luminosity" based on behavioral balance.
     */
    fun identifyQuasars(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        currentTime: Long = System.currentTimeMillis()
    ): List<QuasarState> {
        val window = 30 * 60 * 1000L // 30 minutes
        val logsByStudent = behaviorLogs.filter { currentTime - it.timestamp < window }
            .groupBy { it.studentId }

        return students.mapNotNull { student ->
            val studentLogs = logsByStudent[student.id.toLong()] ?: return@mapNotNull null

            if (studentLogs.size < 3) return@mapNotNull null // Threshold for a Quasar

            val energy = (studentLogs.size.toFloat() / 10f).coerceAtMost(1.0f)

            val positiveCount = studentLogs.count { it.type.contains("Positive", ignoreCase = true) }
            val negativeCount = studentLogs.count { it.type.contains("Negative", ignoreCase = true) }
            val total = (positiveCount + negativeCount).coerceAtLeast(1)

            val behaviorPolarity = (positiveCount - negativeCount).toFloat() / total.toFloat()
            val luminosity = 0.5f + (energy * 0.5f)

            QuasarState(
                studentId = student.id.toLong(),
                x = student.xPosition.value,
                y = student.yPosition.value,
                energy = energy,
                luminosity = luminosity,
                behaviorPolarity = behaviorPolarity
            )
        }
    }
}
