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
     * Calculates the mood for each student based on recent events and academic performance.
     *
     * This method iterates through the provided students and synthesizes their individual
     * [StudentMood] by analyzing their respective behavior, quiz, and homework logs.
     *
     * @param students List of student IDs to evaluate.
     * @param behaviorLogs Map of student IDs to their historical behavior events.
     * @param quizLogs Map of student IDs to their historical quiz results.
     * @param homeworkLogs Map of student IDs to their historical homework logs.
     * @param timeWindow The sliding window (in ms) to consider for "recent" behavior activity.
     *                   Defaults to 15 minutes (900,000ms).
     * @return A list of [StudentMood] objects, one for each student.
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
            moods.add(calculateSingleStudentMood(
                studentId = studentId,
                bLogs = behaviorLogs[studentId],
                qLogs = quizLogs[studentId],
                hLogs = homeworkLogs[studentId],
                now = now,
                timeWindow = timeWindow
            ))
        }

        return moods
    }

    /**
     * BOLT: Optimized single-student mood calculation.
     *
     * This method determines a student's [MoodState], intensity, and valence using a
     * combination of behavioral heuristics and academic momentum.
     *
     * ### Algorithm:
     * 1. **Temporal Filtering**: Scans [BehaviorEvent]s (sorted DESC) and breaks early
     *    once a log exceeds the [timeWindow].
     * 2. **Intensity Synthesis**: Combines behavioral frequency (normalized to 5 logs)
     *    with academic activity (normalized to 10 logs).
     * 3. **Valence Calculation**: Linear ratio of positive to negative logs within the window.
     * 4. **State Transition**:
     *    - **TURBULENT**: More negative logs than positive logs.
     *    - **FOCUSED**: High academic intensity (>0.5) with zero negative logs.
     *    - **ENERGETIC**: High positive log count (>2).
     *    - **CALM**: Baseline state.
     *
     * @param studentId The unique ID of the student.
     * @param bLogs Student's behavior events (assumed DESC sorted).
     * @param qLogs Student's quiz logs.
     * @param hLogs Student's homework logs.
     * @param now Current system time for window comparison.
     * @param timeWindow The active window for behavioral analysis.
     * @return The student's synthesized [StudentMood].
     */
    fun calculateSingleStudentMood(
        studentId: Long,
        bLogs: List<BehaviorEvent>?,
        qLogs: List<QuizLog>?,
        hLogs: List<HomeworkLog>?,
        now: Long = System.currentTimeMillis(),
        timeWindow: Long = 15 * 60 * 1000L
    ): StudentMood {
        var negCount = 0
        var posCount = 0
        var recentBehaviorCount = 0

        if (bLogs != null) {
            // BOLT: Early-exit loop. Behavior logs are sorted DESC (newest first).
            for (j in 0 until bLogs.size) {
                val log = bLogs[j]
                if (now - log.timestamp < timeWindow) {
                    recentBehaviorCount++
                    if (log.type.contains("Negative", ignoreCase = true)) {
                        negCount++
                    } else {
                        posCount++
                    }
                } else {
                    // Logs are sorted newest first, so once we hit one outside the window,
                    // all subsequent logs are also outside.
                    break
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

        return StudentMood(studentId, state, intensity, valence.coerceIn(-1f, 1f))
    }

    /**
     * Calculates the aggregate mood for the entire classroom.
     *
     * This method synthesizes the collective intensity, valence, and stability
     * of the classroom by averaging individual student states.
     *
     * ### Aggregation Logic:
     * - **TURBULENT**: Triggered if > 20% of students are in a TURBULENT state.
     * - **FOCUSED**: Triggered if > 40% of students are in a FOCUSED state.
     * - **ENERGETIC**: Triggered if > 30% of students are in an ENERGETIC state.
     * - **CALM**: The default collective state.
     * - **Stability**: Calculated as 1.0 minus the ratio of turbulent students.
     *
     * @param studentMoods List of individual student mood objects.
     * @return A [ClassroomMood] representing the collective atmosphere.
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
