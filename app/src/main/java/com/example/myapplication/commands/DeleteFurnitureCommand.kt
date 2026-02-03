package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to remove a furniture item from the seating chart.
 * Reversing this command re-inserts the furniture back into the database.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param furniture The [Furniture] entity to be deleted.
 */
class DeleteFurnitureCommand(
    private val viewModel: SeatingChartViewModel,
    private val furniture: Furniture
) : Command {
    override suspend fun execute() {
        viewModel.internalDeleteFurniture(furniture)
    }

    override suspend fun undo() {
        viewModel.internalAddFurniture(furniture)
    }

    override fun getDescription(): String = "Delete furniture: ${furniture.name}"
}
