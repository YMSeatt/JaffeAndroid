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

    /**
     * Represents a connection between two synergistic students.
     *
     * @property studentA The Database ID of the first student.
     * @property studentB The Database ID of the second student.
     * @property strength The normalized intensity of the connection (0.0 to 1.0).
     * @property type The classification of synergy (Academic vs. Homework).
     */
    data class NeuralThread(
        val studentA: Long,
        val studentB: Long,
        val strength: Float, // 0.0 to 1.0 based on number of shared milestones
        val type: ThreadType
    )

    /**
     * Defines the source of the neural connection.
     */
    enum class ThreadType {
        /** Based on shared high-performance (>=80%) on quiz assessments. */
        ACADEMIC_SYNERGY,
        /** Based on shared completion of homework assignments. */
        HOMEWORK_COLLABORATION
    }

    /**
     * Identifies neural threads based on academic performance and homework completion.
     *
     * ### Algorithm Logic:
     * 1. **Pre-processing (Stage 1)**: Aggregates logs into student-keyed [Set]s of milestone names.
     *    - Quizzes are included if score is >= 80%.
     *    - Homework is included if status indicates completion (excluding "Not Done").
     * 2. **Pairwise Comparison (Stage 2)**: Intersects the milestone sets for every student pair.
     * 3. **Strength Mapping**: Normalizes the intersection count into a [strength] float.
     *
     * ### BOLT (Performance-Obsessed) Optimization:
     * - **Complexity**: Reduces nested log scans from $O(S^2 \times L^2)$ to $O(S^2 \times L_{shared})$
     *   by using O(1) Set lookups instead of repeated list iterations.
     * - **Allocation**: Uses manual loop indexing to minimize iterator churn in high-frequency paths.
     *
     * @param quizLogsByStudent Raw quiz logs grouped by student ID.
     * @param homeworkLogsByStudent Raw homework logs grouped by student ID.
     * @return A list of identified [NeuralThread] objects.
     */
    fun identifyThreads(
        quizLogsByStudent: Map<Long, List<QuizLog>>,
        homeworkLogsByStudent: Map<Long, List<HomeworkLog>>
    ): List<NeuralThread> {
        val threads = mutableListOf<NeuralThread>()

        val studentIdsList = mutableListOf<Long>()
        val highPerformingQuizzes = mutableMapOf<Long, Set<String>>()
        val completedHomework = mutableMapOf<Long, Set<String>>()

        // Pre-process Quiz Logs: O(S * Lq)
        for ((id, logs) in quizLogsByStudent) {
            studentIdsList.add(id)
            val quizSet = mutableSetOf<String>()
            for (i in logs.indices) {
                val q = logs[i]
                val qAValue = q.markValue ?: 0.0
                val qAMax = q.maxMarkValue ?: 0.0
                val score = if (qAMax > 0) qAValue / qAMax else 0.0
                if (score >= 0.8) {
                    quizSet.add(q.quizName)
                }
            }
            if (quizSet.isNotEmpty()) {
                highPerformingQuizzes[id] = quizSet
            }
        }

        // Pre-process Homework Logs: O(S * Lh)
        for ((id, logs) in homeworkLogsByStudent) {
            if (!quizLogsByStudent.containsKey(id)) {
                studentIdsList.add(id)
            }
            val hwSet = mutableSetOf<String>()
            for (i in logs.indices) {
                val h = logs[i]
                val status = h.status
                // BOLT: Refined check to avoid "Not Done" false positives.
                val isDone = (status.contains("Done", ignoreCase = true) && !status.contains("Not", ignoreCase = true))
                             || status.contains("Complete", ignoreCase = true)
                if (isDone) {
                    hwSet.add(h.assignmentName)
                }
            }
            if (hwSet.isNotEmpty()) {
                completedHomework[id] = hwSet
            }
        }

        // Pairwise student comparison: O(S^2 * L_shared)
        for (i in 0 until studentIdsList.size) {
            val idA = studentIdsList[i]
            val quizzesA = highPerformingQuizzes[idA]
            val homeworkA = completedHomework[idA]

            for (j in i + 1 until studentIdsList.size) {
                val idB = studentIdsList[j]

                // 1. Check shared high-performing quizzes
                val quizzesB = highPerformingQuizzes[idB]
                if (quizzesA != null && quizzesB != null) {
                    var sharedQuizCount = 0
                    if (quizzesA.size < quizzesB.size) {
                        for (qName in quizzesA) {
                            if (quizzesB.contains(qName)) sharedQuizCount++
                        }
                    } else {
                        for (qName in quizzesB) {
                            if (quizzesA.contains(qName)) sharedQuizCount++
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
                }

                // 2. Check shared completed homework
                val homeworkB = completedHomework[idB]
                if (homeworkA != null && homeworkB != null) {
                    var sharedHomeworkCount = 0
                    if (homeworkA.size < homeworkB.size) {
                        for (hName in homeworkA) {
                            if (homeworkB.contains(hName)) sharedHomeworkCount++
                        }
                    } else {
                        for (hName in homeworkB) {
                            if (homeworkA.contains(hName)) sharedHomeworkCount++
                        }
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
        }

        return threads
    }
}
