package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to update a furniture item's information or styling.
 * Stores a copy of both the old and new furniture states to allow reverting changes.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param oldFurniture The furniture state before the update.
 * @param newFurniture The furniture state after the update.
 */
class UpdateFurnitureCommand(
    private val viewModel: SeatingChartViewModel,
    private val oldFurniture: Furniture,
    private val newFurniture: Furniture
) : Command {
    override suspend fun execute() {
        viewModel.internalUpdateFurniture(newFurniture)
    }

    override suspend fun undo() {
        viewModel.internalUpdateFurniture(oldFurniture)
    }

    override fun getDescription(): String = "Update furniture: ${newFurniture.name}"
}
