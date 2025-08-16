package com.example.myapplication.commands

import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.viewmodel.SeatingChartViewModel

class LogHomeworkCommand(
    private val viewModel: SeatingChartViewModel,
    private val log: HomeworkLog
) : Command {
    override fun execute() {
        viewModel.internalAddHomeworkLog(log)
    }

    override fun undo() {
        viewModel.deleteHomeworkLog(log)
    }
}
