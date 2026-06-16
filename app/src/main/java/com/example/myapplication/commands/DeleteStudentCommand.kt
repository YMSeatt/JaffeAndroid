package com.example.myapplication.commands

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to remove a student from the seating chart.
 *
 * This is a "High-Integrity" command ported from the Python blueprint. When a student is deleted,
 * it captures a complete snapshot of all associated behavioral, quiz, and homework logs.
 * This ensures that undoing a deletion restores the student's entire history,
 * maintaining data continuity despite database cascade deletions.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param student The [Student] entity to be deleted.
 * @param behaviorLogs Historical behavior events for this student.
 * @param quizLogs Historical quiz performance logs for this student.
 * @param homeworkLogs Historical homework completion logs for this student.
 */
class DeleteStudentCommand(
    internal val viewModel: SeatingChartViewModel,
    internal val student: Student,
    private val behaviorLogs: List<BehaviorEvent> = emptyList(),
    private val quizLogs: List<QuizLog> = emptyList(),
    private val homeworkLogs: List<HomeworkLog> = emptyList()
) : Command {
    override suspend fun execute() {
        viewModel.internalDeleteStudent(student)
    }

    override suspend fun undo() {
        // Step 1: Restore the student record first to satisfy foreign key constraints.
        viewModel.internalAddStudent(student)

        // Step 2: Restore all historical logs in bulk.
        if (behaviorLogs.isNotEmpty()) {
            viewModel.internalAddBehaviorEvents(behaviorLogs)
        }
        if (quizLogs.isNotEmpty()) {
            viewModel.internalAddQuizLogs(quizLogs)
        }
        if (homeworkLogs.isNotEmpty()) {
            viewModel.internalAddHomeworkLogs(homeworkLogs)
        }
    }

    override fun getDescription(): String = "Delete student: ${student.firstName} ${student.lastName}"
}
