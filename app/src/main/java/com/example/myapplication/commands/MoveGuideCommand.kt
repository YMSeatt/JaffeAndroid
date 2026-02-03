package com.example.myapplication.commands

import com.example.myapplication.data.Guide
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to move an alignment guide to a new position.
 * Captures both the old and new positions to support undo/redo.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param guide The [Guide] entity being moved.
 * @param oldPosition The position (X for vertical, Y for horizontal) before the move.
 * @param newPosition The position after the move.
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
