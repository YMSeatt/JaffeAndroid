package com.example.myapplication.commands

import com.example.myapplication.viewmodel.SeatingChartViewModel

class MoveStudentCommand(
    private val viewModel: SeatingChartViewModel,
    private val studentId: Int,
    private val oldX: Float,
    private val oldY: Float,
    private val newX: Float,
    private val newY: Float
) : Command {
    override suspend fun execute() {
        viewModel.internalUpdateStudentPosition(studentId.toLong(), newX, newY)
    }

    override suspend fun undo() {
        viewModel.internalUpdateStudentPosition(studentId.toLong(), oldX, oldY)
    }

    override fun getDescription(): String = "Move student (ID: $studentId)"
}
