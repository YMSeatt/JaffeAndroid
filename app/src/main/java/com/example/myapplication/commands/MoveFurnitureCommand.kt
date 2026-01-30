package com.example.myapplication.commands

import com.example.myapplication.viewmodel.SeatingChartViewModel

class MoveFurnitureCommand(
    private val viewModel: SeatingChartViewModel,
    private val furnitureId: Int,
    private val oldX: Float,
    private val oldY: Float,
    private val newX: Float,
    private val newY: Float
) : Command {
    override suspend fun execute() {
        viewModel.internalUpdateFurniturePosition(furnitureId.toLong(), newX, newY)
    }

    override suspend fun undo() {
        viewModel.internalUpdateFurniturePosition(furnitureId.toLong(), oldX, oldY)
    }

    override fun getDescription(): String = "Moved Furniture (ID: $furnitureId)"
}
