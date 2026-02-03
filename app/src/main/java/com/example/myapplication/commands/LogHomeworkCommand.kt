package com.example.myapplication.commands

import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to record a student's homework completion status.
 * Reversing this command removes the homework log entry from the database.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param log The [HomeworkLog] to be recorded.
 */
class LogHomeworkCommand(
    private val viewModel: SeatingChartViewModel,
    private val log: HomeworkLog
) : Command {
    override suspend fun execute() {
        viewModel.internalAddHomeworkLog(log)
    }

    override suspend fun undo() {
        viewModel.deleteHomeworkLog(log)
    }

    override fun getDescription(): String = "Log homework: ${log.assignmentName}"
}
