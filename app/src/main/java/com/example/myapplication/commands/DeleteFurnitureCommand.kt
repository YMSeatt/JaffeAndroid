package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.viewmodel.SeatingChartViewModel

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
