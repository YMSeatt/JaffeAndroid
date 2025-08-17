package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

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
}
