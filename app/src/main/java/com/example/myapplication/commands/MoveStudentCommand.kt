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
    override fun execute() {
        viewModel.updateStudentPosition(studentId, oldX, oldY, newX, newY)
    }

    override fun undo() {
        viewModel.updateStudentPosition(studentId, newX, newY, oldX, oldY)
    }
}
