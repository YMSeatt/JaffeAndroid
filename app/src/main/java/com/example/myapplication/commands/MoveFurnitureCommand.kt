package com.example.myapplication.commands

import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to move a furniture item to a new position on the seating chart.
 * Captures both the old and new coordinates to support full undo/redo of movement.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param furnitureId The unique ID of the furniture item being moved.
 * @param oldX The horizontal position before the move.
 * @param oldY The vertical position before the move.
 * @param newX The horizontal position after the move.
 * @param newY The vertical position after the move.
 */
class MoveFurnitureCommand(
    private val viewModel: SeatingChartViewModel,
    private val furnitureId: Int,
    private val oldX: Float,
    private val oldY: Float,
    private val newX: Float,
    private val newY: Float
) : Command {
    override suspend fun execute() {
        viewModel.internalUpdateFurniturePosition(furnitureId.toLong(), newX, newY)
    }

    override suspend fun undo() {
        viewModel.internalUpdateFurniturePosition(furnitureId.toLong(), oldX, oldY)
    }

    override fun getDescription(): String = "Move furniture (ID: $furnitureId)"
}
