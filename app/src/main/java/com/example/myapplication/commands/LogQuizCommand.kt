package com.example.myapplication.commands

import com.example.myapplication.data.QuizLog
import com.example.myapplication.viewmodel.SeatingChartViewModel

class LogQuizCommand(
    private val viewModel: SeatingChartViewModel,
    private val log: QuizLog
) : Command {
    override suspend fun execute() {
        viewModel.internalSaveQuizLog(log)
    }

    override suspend fun undo() {
        viewModel.deleteQuizLog(log)
    }

    override fun getDescription(): String = "Log quiz: ${log.quizName}"
}
