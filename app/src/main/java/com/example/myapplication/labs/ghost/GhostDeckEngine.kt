package com.example.myapplication.labs.ghost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostDeckEngine: Neural Student Card Stack Logic.
 *
 * This engine manages the state of the "Ghost Deck" and calculates "Neural Affinity"
 * for each student to drive card visuals and sorting.
 */
object GhostDeckEngine {

    /**
     * Represents the synthesized state of a student card in the deck.
     */
    data class StudentCard(
        val student: StudentUiItem,
        val affinity: Float, // 0.0 to 1.0 (Higher means more "Turbulent/Friction")
        val focusNeeded: Boolean,
        val engagementScore: Float
    )

    /**
     * Synthesizes a list of [StudentCard]s from the current classroom state.
     *
     * BOLT ⚡ Optimization: Uses manual loops and efficient grouping to minimize O(N) overhead.
     */
    fun createDeck(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): List<StudentCard> {
        val deck = ArrayList<StudentCard>(students.size)

        // Group logs by studentId for efficient lookup
        val bMap = behaviorLogs.groupBy { it.studentId }
        val qMap = quizLogs.groupBy { it.studentId }
        val hMap = homeworkLogs.groupBy { it.studentId }

        for (i in students.indices) {
            val student = students[i]
            val sId = student.id.toLong()

            val sBehavior = bMap[sId] ?: emptyList()
            val sQuiz = qMap[sId] ?: emptyList()
            val sHomework = hMap[sId] ?: emptyList()

            val affinity = calculateAffinity(sBehavior)
            val engagement = calculateEngagement(sQuiz, sHomework)

            deck.add(
                StudentCard(
                    student = student,
                    affinity = affinity,
                    focusNeeded = affinity > 0.6f || (engagement < 0.3f && sBehavior.isNotEmpty()),
                    engagementScore = engagement
                )
            )
        }

        // BOLT: Sort by affinity (priority) then engagement (secondary)
        deck.sortWith(compareByDescending<StudentCard> { it.affinity }.thenBy { it.engagementScore })

        return deck
    }

    private fun calculateAffinity(behavior: List<BehaviorEvent>): Float {
        if (behavior.isEmpty()) return 0.0f // Neutral/No friction
        var negativeCount = 0
        for (j in behavior.indices) {
            if (behavior[j].type.contains("Negative", ignoreCase = true)) {
                negativeCount++
            }
        }
        return (negativeCount.toFloat() / behavior.size.coerceAtLeast(1)).coerceIn(0f, 1f)
    }

    private fun calculateEngagement(quiz: List<QuizLog>, homework: List<HomeworkLog>): Float {
        val totalAcademic = quiz.size + homework.size
        return (totalAcademic.toFloat() / 8f).coerceIn(0f, 1f)
    }
}
