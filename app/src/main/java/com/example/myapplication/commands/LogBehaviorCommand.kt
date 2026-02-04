package com.example.myapplication.commands

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to record a student behavior event.
 * Reversing this command removes the event from the student's behavior history.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param event The [BehaviorEvent] to be recorded.
 */
class LogBehaviorCommand(
    private val viewModel: SeatingChartViewModel,
    private val event: BehaviorEvent
) : Command {
    private var generatedId: Int = event.id

    override suspend fun execute() {
        val eventToInsert = if (generatedId != 0) event.copy(id = generatedId) else event
        generatedId = viewModel.internalAddBehaviorEvent(eventToInsert).toInt()
    }

    override suspend fun undo() {
        viewModel.deleteBehaviorEvent(event.copy(id = generatedId))
    }

    override fun getDescription(): String = "Log behavior: ${event.type}"
}
