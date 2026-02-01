package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

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
