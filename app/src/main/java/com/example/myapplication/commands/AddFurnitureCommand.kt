package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

class AddFurnitureCommand(
    private val viewModel: SeatingChartViewModel,
    private val furniture: Furniture
) : Command {
    override fun execute() {
        viewModel.addFurniture(furniture)
    }

    override fun undo() {
        viewModel.deleteFurnitureById(furniture.id)
    }
}
