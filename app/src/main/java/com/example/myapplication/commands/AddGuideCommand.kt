package com.example.myapplication.commands

import com.example.myapplication.data.Guide
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to add a visual alignment guide (vertical or horizontal) to the seating chart.
 * Captures the generated ID during execution to ensure [undo] targets the correct record.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param guide The [Guide] entity to be added.
 */
class AddGuideCommand(
    private val viewModel: SeatingChartViewModel,
    private val guide: Guide
) : Command {
    private var generatedId: Long = guide.id

    override suspend fun execute() {
        val guideToInsert = if (generatedId != 0L) guide.copy(id = generatedId) else guide
        generatedId = viewModel.internalAddGuide(guideToInsert)
    }

    override suspend fun undo() {
        viewModel.internalDeleteGuide(guide.copy(id = generatedId))
    }

    override fun getDescription(): String = "Add ${guide.type.name.lowercase()} guide at ${guide.position}"
}
