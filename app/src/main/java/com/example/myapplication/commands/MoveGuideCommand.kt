package com.example.myapplication.commands

import com.example.myapplication.data.Guide
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to move a guide.
 * Ports logic from Python's MoveGuideCommand.
 */
class MoveGuideCommand(
    private val viewModel: SeatingChartViewModel,
    private val guide: Guide,
    private val oldPosition: Float,
    private val newPosition: Float
) : Command {
    override suspend fun execute() {
        viewModel.internalUpdateGuide(guide.copy(position = newPosition))
    }

    override suspend fun undo() {
        viewModel.internalUpdateGuide(guide.copy(position = oldPosition))
    }

    override fun getDescription(): String = "Move ${guide.type.name.lowercase()} guide from $oldPosition to $newPosition"
}
