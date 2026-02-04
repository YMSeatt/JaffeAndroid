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
    private var generatedId: Int = furniture.id

    override suspend fun execute() {
        val furnitureToInsert = if (generatedId != 0) furniture.copy(id = generatedId) else furniture
        generatedId = viewModel.internalAddFurniture(furnitureToInsert).toInt()
    }

    override suspend fun undo() {
        viewModel.internalDeleteFurniture(furniture.copy(id = generatedId))
    }

    override fun getDescription(): String = "Add furniture: ${furniture.name}"
}
