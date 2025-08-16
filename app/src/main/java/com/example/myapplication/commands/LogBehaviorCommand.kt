package com.example.myapplication.commands

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.viewmodel.SeatingChartViewModel

class LogBehaviorCommand(
    private val viewModel: SeatingChartViewModel,
    private val event: BehaviorEvent
) : Command {
    override fun execute() {
        viewModel.addBehaviorEvent(event)
    }

    override fun undo() {
        viewModel.deleteBehaviorEvent(event)
    }
}
