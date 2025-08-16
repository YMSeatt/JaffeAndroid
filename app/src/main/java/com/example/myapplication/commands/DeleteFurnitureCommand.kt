package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

class DeleteFurnitureCommand(
    private val viewModel: SeatingChartViewModel,
    private val furniture: Furniture
) : Command {
    override fun execute() {
        viewModel.deleteFurnitureById(furniture.id)
    }

    override fun undo() {
        viewModel.addFurniture(furniture)
    }
}
