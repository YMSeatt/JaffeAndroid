package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

class UpdateFurnitureCommand(
    private val viewModel: SeatingChartViewModel,
    private val oldFurniture: Furniture,
    private val newFurniture: Furniture
) : Command {
    override fun execute() {
        viewModel.updateFurniture(newFurniture)
    }

    override fun undo() {
        viewModel.updateFurniture(oldFurniture)
    }
}
