package com.example.myapplication.labs.ghost.beacon

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.random.Random

/**
 * GhostBeaconEngine: Data-driven neural student selection engine.
 *
 * This engine calculates a "Need for Interaction" (NFI) score for each student
 * based on behavioral and academic metrics, then performs a weighted random
 * selection to pick a "Beacon Target".
 */
object GhostBeaconEngine {

    /**
     * Identifies a student who requires immediate attention or interaction.
     *
     * @param students The list of students to consider.
     * @param behaviorLogs All behavior logs for the current context.
     * @param quizLogs All quiz logs for the current context.
     * @param homeworkLogs All homework logs for the current context.
     * @return The ID of the selected student, or null if no students exist.
     */
    fun pickBeaconTarget(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): Long? {
        if (students.isEmpty()) return null

        val now = System.currentTimeMillis()
        val studentWeights = mutableMapOf<Long, Float>()

        // BOLT: Single-pass log aggregation to minimize iteration.
        val negativeCounts = mutableMapOf<Long, Int>()
        val lastPositiveLog = mutableMapOf<Long, Long>()
        val academicScores = mutableMapOf<Long, MutableList<Double>>()

        for (log in behaviorLogs) {
            val sid = log.studentId
            if (log.type.contains("Negative", ignoreCase = true)) {
                negativeCounts[sid] = (negativeCounts[sid] ?: 0) + 1
            } else {
                val last = lastPositiveLog[sid] ?: 0L
                if (log.timestamp > last) {
                    lastPositiveLog[sid] = log.timestamp
                }
            }
        }

        for (log in quizLogs) {
            val sid = log.studentId
            val percentage = if (log.maxMarkValue > 0.0) (log.markValue.toDouble() / log.maxMarkValue.toDouble()) else 0.0
            academicScores.getOrPut(sid) { mutableListOf() }.add(percentage)
        }

        for (log in homeworkLogs) {
            // Homework logs don't always have a direct percentage, but we can infer effort.
            // Simplified for PoC.
        }

        var totalWeight = 0f
        val weightedStudents = mutableListOf<Pair<Long, Float>>()

        for (student in students) {
            val sid = student.id.toLong()
            var nfi = 1.0f // Base weight

            // Factor 1: Negative Behavior (Heavy weight)
            val negs = negativeCounts[sid] ?: 0
            nfi += negs * 2.0f

            // Factor 2: Lack of Positive Interaction (Time-based decay)
            val lastPos = lastPositiveLog[sid] ?: 0L
            val hoursSincePositive = if (lastPos == 0L) 24f else (now - lastPos).toFloat() / (1000f * 60f * 60f)
            nfi += (hoursSincePositive / 2f).coerceAtMost(5.0f)

            // Factor 3: Academic Performance (Inversely proportional)
            val scores = academicScores[sid]
            if (scores != null && scores.isNotEmpty()) {
                val avg = scores.average().toFloat()
                nfi += (1.0f - avg) * 3.0f
            } else {
                nfi += 1.0f // Slight boost for students with no academic data
            }

            studentWeights[sid] = nfi
            totalWeight += nfi
            weightedStudents.add(sid to nfi)
        }

        // Weighted random selection
        var randomValue = Random.nextFloat() * totalWeight
        for (pair in weightedStudents) {
            randomValue -= pair.second
            if (randomValue <= 0) return pair.first
        }

        return students.first().id.toLong()
    }
}
