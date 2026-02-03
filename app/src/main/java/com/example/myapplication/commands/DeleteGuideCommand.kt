package com.example.myapplication.commands

import com.example.myapplication.data.Guide
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to remove an alignment guide from the seating chart.
 * Reversing this command re-inserts the guide back into the database.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param guide The [Guide] entity to be deleted.
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
