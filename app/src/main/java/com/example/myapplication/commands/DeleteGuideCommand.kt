package com.example.myapplication.commands

import com.example.myapplication.data.Guide
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to delete a guide.
 * Ports logic from Python's DeleteGuideCommand.
 */
class DeleteGuideCommand(
    private val viewModel: SeatingChartViewModel,
    private val guide: Guide
) : Command {
    override suspend fun execute() {
        viewModel.internalDeleteGuide(guide)
    }

    override suspend fun undo() {
        viewModel.internalAddGuide(guide)
    }

    override fun getDescription(): String = "Delete ${guide.type.name.lowercase()} guide at ${guide.position}"
}
