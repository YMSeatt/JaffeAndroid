package com.example.myapplication.commands

import com.example.myapplication.data.Guide
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to add a new guide.
 * Ports logic from Python's AddGuideCommand.
 */
class AddGuideCommand(
    private val viewModel: SeatingChartViewModel,
    private val guide: Guide
) : Command {
    private var insertedGuide: Guide? = null

    override suspend fun execute() {
        val id = viewModel.internalAddGuide(guide)
        insertedGuide = guide.copy(id = id)
    }

    override suspend fun undo() {
        insertedGuide?.let {
            viewModel.internalDeleteGuide(it)
        }
    }

    override fun getDescription(): String = "Add ${guide.type.name.lowercase()} guide at ${guide.position}"
}
