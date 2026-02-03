package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to add a new furniture item to the seating chart.
 * Reversing this command removes the furniture from the database.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param furniture The [Furniture] entity to be added.
 */
class AddFurnitureCommand(
    private val viewModel: SeatingChartViewModel,
    private val furniture: Furniture
) : Command {
    override suspend fun execute() {
        viewModel.internalAddFurniture(furniture)
    }

    override suspend fun undo() {
        viewModel.internalDeleteFurniture(furniture)
    }

    override fun getDescription(): String = "Add furniture: ${furniture.name}"
}
