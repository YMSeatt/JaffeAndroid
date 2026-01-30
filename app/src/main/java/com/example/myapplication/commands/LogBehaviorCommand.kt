package com.example.myapplication.commands

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.viewmodel.SeatingChartViewModel

class LogBehaviorCommand(
    private val viewModel: SeatingChartViewModel,
    private val event: BehaviorEvent
) : Command {
    override suspend fun execute() {
        viewModel.internalAddBehaviorEvent(event)
    }

    override suspend fun undo() {
        viewModel.deleteBehaviorEvent(event)
    }

    override fun getDescription(): String = "Logged Behavior: ${event.type} for Student ${event.studentId}"
}
