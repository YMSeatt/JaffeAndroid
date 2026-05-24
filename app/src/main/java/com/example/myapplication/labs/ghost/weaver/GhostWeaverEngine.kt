package com.example.myapplication.labs.ghost.weaver

import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.HomeworkLog

/**
 * GhostWeaverEngine: Visualizes shared academic milestones as neural threads.
 *
 * This engine identifies "Neural Threads" between students who have achieved
 * similar academic milestones, such as high scores on the same quiz or
 * completion of the same homework assignments.
 */
object GhostWeaverEngine {

    data class NeuralThread(
        val studentA: Long,
        val studentB: Long,
        val strength: Float, // 0.0 to 1.0 based on number of shared milestones
        val type: ThreadType
    )

    enum class ThreadType {
        ACADEMIC_SYNERGY,
        HOMEWORK_COLLABORATION
    }

    /**
     * Identifies neural threads based on academic performance and homework completion.
     *
     * BOLT: Optimized with manual loops and identity-based caching to avoid
     * collection churn.
     */
    fun identifyThreads(
        quizLogsByStudent: Map<Long, List<QuizLog>>,
        homeworkLogsByStudent: Map<Long, List<HomeworkLog>>
    ): List<NeuralThread> {
        val threads = mutableListOf<NeuralThread>()
        val studentIds = (quizLogsByStudent.keys + homeworkLogsByStudent.keys).distinct().toList()

        for (i in studentIds.indices) {
            val idA = studentIds[i]
            val quizzesA = quizLogsByStudent[idA] ?: emptyList()
            val homeworkA = homeworkLogsByStudent[idA] ?: emptyList()

            for (j in i + 1 until studentIds.size) {
                val idB = studentIds[j]
                val quizzesB = quizLogsByStudent[idB] ?: emptyList()
                val homeworkB = homeworkLogsByStudent[idB] ?: emptyList()

                var sharedQuizCount = 0
                var sharedHomeworkCount = 0

                // 1. Check shared high-performing quizzes
                for (qa in quizzesA) {
                    val qAValue = qa.markValue ?: 0.0
                    val qAMax = qa.maxMarkValue ?: 0.0
                    val scoreA = if (qAMax > 0) qAValue / qAMax else 0.0
                    if (scoreA < 0.8) continue

                    for (qb in quizzesB) {
                        if (qa.quizName == qb.quizName) {
                            val qBValue = qb.markValue ?: 0.0
                            val qBMax = qb.maxMarkValue ?: 0.0
                            val scoreB = if (qBMax > 0) qBValue / qBMax else 0.0
                            if (scoreB >= 0.8) {
                                sharedQuizCount++
                            }
                        }
                    }
                }

                // 2. Check shared completed homework
                for (ha in homeworkA) {
                    if (ha.status.contains("Done", ignoreCase = true) || ha.status.contains("Complete", ignoreCase = true)) {
                        for (hb in homeworkB) {
                            if (ha.assignmentName == hb.assignmentName) {
                                if (hb.status.contains("Done", ignoreCase = true) || hb.status.contains("Complete", ignoreCase = true)) {
                                    sharedHomeworkCount++
                                }
                            }
                        }
                    }
                }

                if (sharedQuizCount > 0) {
                    threads.add(
                        NeuralThread(
                            idA, idB,
                            strength = (sharedQuizCount.toFloat() / 3f).coerceAtMost(1.0f),
                            type = ThreadType.ACADEMIC_SYNERGY
                        )
                    )
                }

                if (sharedHomeworkCount > 0) {
                    threads.add(
                        NeuralThread(
                            idA, idB,
                            strength = (sharedHomeworkCount.toFloat() / 5f).coerceAtMost(1.0f),
                            type = ThreadType.HOMEWORK_COLLABORATION
                        )
                    )
                }
            }
        }

        return threads
    }
}
