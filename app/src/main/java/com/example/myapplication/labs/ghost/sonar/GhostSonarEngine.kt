package com.example.myapplication.labs.ghost.sonar

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem

/**
 * GhostSonarEngine: Spatial Engagement Discovery.
 *
 * This engine identifies "Quiet Zones" in the classroom by detecting students
 * who haven't had any behavioral or academic logs within a specific time window.
 *
 * ### Performance Context (BOLT):
 * Uses O(Recent) analysis with early-exit loops and manual indexing to ensure
 * zero-allocation 60fps performance during sonar sweeps.
 */
object GhostSonarEngine {

    /**
     * Identifies "Quiet" students who require engagement by scanning academic and behavioral history.
     *
     * A student is considered "quiet" if they have zero recorded events within the specified
     * [timeWindowMs]. This helps teachers identify students who may be slipping through the
     * cracks or who haven't received attention in the current session.
     *
     * @param students The list of students to evaluate.
     * @param behaviorLogs Historical behavior logs. Must be DESC sorted for O(Recent) performance.
     * @param quizLogs Historical quiz logs.
     * @param homeworkLogs Historical homework logs.
     * @param timeWindowMs The window to consider for "recent" activity (default 10 mins).
     * @return Set of student IDs who are "quiet".
     */
    fun identifyQuietStudents(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>,
        timeWindowMs: Long = 10 * 60 * 1000L
    ): Set<Long> {
        val now = System.currentTimeMillis()
        val cutoff = now - timeWindowMs

        // BOLT: Use a HashSet for O(1) lookups of students with recent activity
        val activeStudentIds = HashSet<Long>(students.size)

        // 1. Scan Behavior Logs (O(Recent) early exit)
        for (i in 0 until behaviorLogs.size) {
            val log = behaviorLogs[i]
            if (log.timestamp < cutoff) break
            activeStudentIds.add(log.studentId)
        }

        // 2. Scan Quiz Logs
        for (i in 0 until quizLogs.size) {
            val log = quizLogs[i]
            if (log.timestamp < cutoff) continue // Quiz logs might not be perfectly sorted by timestamp in all views
            activeStudentIds.add(log.studentId)
        }

        // 3. Scan Homework Logs
        for (i in 0 until homeworkLogs.size) {
            val log = homeworkLogs[i]
            if (log.timestamp < cutoff) continue
            activeStudentIds.add(log.studentId)
        }

        // 4. Identify quiet students
        val quietStudentIds = HashSet<Long>()
        for (i in 0 until students.size) {
            val studentId = students[i].id.toLong()
            if (!activeStudentIds.contains(studentId)) {
                quietStudentIds.add(studentId)
            }
        }

        return quietStudentIds
    }
}
