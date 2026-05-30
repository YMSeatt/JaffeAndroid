package com.example.myapplication.labs.ghost.mood

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog

/**
 * GhostMoodEngine: Analyzes classroom data to determine "mood" states.
 *
 * This engine synthesizes behavioral and academic data into organic "mood" parameters
 * used for atmospheric visualization.
 *
 * BOLT ⚡ Optimization: Uses manual index-based loops and O(N) single-pass analysis
 * to eliminate iterator allocations and minimize GC pressure.
 */
object GhostMoodEngine {

    enum class MoodState {
        CALM,       // Stable, balanced activity
        FOCUSED,    // High academic engagement, low behavioral turbulence
        TURBULENT,  // High frequency of negative logs or rapid changes
        ENERGETIC   // High frequency of positive logs
    }

    data class StudentMood(
        val studentId: Long,
        val state: MoodState,
        val intensity: Float, // 0.0 to 1.0
        val valence: Float    // -1.0 (Negative) to 1.0 (Positive)
    )

    data class ClassroomMood(
        val aggregateState: MoodState,
        val collectiveIntensity: Float,
        val collectiveValence: Float,
        val stability: Float // 0.0 (Chaotic) to 1.0 (Stable)
    )

    /**
     * Calculates the mood for each student based on recent events.
     *
     * @param students List of student IDs.
     * @param behaviorLogs Map of student IDs to their behavior events.
     * @param quizLogs Map of student IDs to their quiz results.
     * @param homeworkLogs Map of student IDs to their homework logs.
     * @param timeWindow The window (in ms) to consider for "recent" behavior activity.
     */
    fun calculateStudentMoods(
        students: List<Long>,
        behaviorLogs: Map<Long, List<BehaviorEvent>>,
        quizLogs: Map<Long, List<QuizLog>>,
        homeworkLogs: Map<Long, List<HomeworkLog>>,
        timeWindow: Long = 15 * 60 * 1000L // 15 minutes
    ): List<StudentMood> {
        val now = System.currentTimeMillis()
        val moods = ArrayList<StudentMood>(students.size)

        for (i in 0 until students.size) {
            val studentId = students[i]
            val bLogs = behaviorLogs[studentId]
            val qLogs = quizLogs[studentId]
            val hLogs = homeworkLogs[studentId]

            var negCount = 0
            var posCount = 0
            var recentBehaviorCount = 0

            if (bLogs != null) {
                for (j in 0 until bLogs.size) {
                    val log = bLogs[j]
                    if (now - log.timestamp < timeWindow) {
                        recentBehaviorCount++
                        if (log.type.contains("Negative", ignoreCase = true)) {
                            negCount++
                        } else {
                            posCount++
                        }
                    }
                }
            }

            val quizCount = qLogs?.size ?: 0
            val hwCount = hLogs?.size ?: 0
            val academicIntensity = (quizCount + hwCount).toFloat() / 10f
            val behaviorIntensity = recentBehaviorCount.toFloat() / 5f
            val intensity = (academicIntensity + behaviorIntensity).coerceIn(0.1f, 1.0f)

            val valence = if (recentBehaviorCount > 0) {
                (posCount - negCount).toFloat() / recentBehaviorCount.toFloat()
            } else 0f

            val state = when {
                negCount > posCount -> MoodState.TURBULENT
                academicIntensity > 0.5f && negCount == 0 -> MoodState.FOCUSED
                posCount > 2 -> MoodState.ENERGETIC
                else -> MoodState.CALM
            }

            moods.add(StudentMood(studentId, state, intensity, valence.coerceIn(-1f, 1f)))
        }

        return moods
    }

    /**
     * Calculates the overall classroom mood based on student moods.
     */
    fun calculateClassroomMood(studentMoods: List<StudentMood>): ClassroomMood {
        if (studentMoods.isEmpty()) {
            return ClassroomMood(MoodState.CALM, 0.2f, 0f, 1.0f)
        }

        var totalIntensity = 0f
        var totalValence = 0f
        var turbulentCount = 0
        var focusedCount = 0
        var energeticCount = 0

        for (i in 0 until studentMoods.size) {
            val mood = studentMoods[i]
            totalIntensity += mood.intensity
            totalValence += mood.valence
            when (mood.state) {
                MoodState.TURBULENT -> turbulentCount++
                MoodState.FOCUSED -> focusedCount++
                MoodState.ENERGETIC -> energeticCount++
                else -> {}
            }
        }

        val count = studentMoods.size.toFloat()
        val avgIntensity = totalIntensity / count
        val avgValence = totalValence / count

        val state = when {
            turbulentCount > studentMoods.size * 0.2 -> MoodState.TURBULENT
            focusedCount > studentMoods.size * 0.4 -> MoodState.FOCUSED
            energeticCount > studentMoods.size * 0.3 -> MoodState.ENERGETIC
            else -> MoodState.CALM
        }

        val stability = 1.0f - (turbulentCount.toFloat() / count)

        return ClassroomMood(state, avgIntensity, avgValence, stability)
    }
}
