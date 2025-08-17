package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

class UpdateStudentCommand(
    private val viewModel: SeatingChartViewModel,
    private val oldStudent: Student,
    private val newStudent: Student
) : Command {
    override suspend fun execute() {
        viewModel.internalUpdateStudent(newStudent)
    }

    override suspend fun undo() {
        viewModel.internalUpdateStudent(oldStudent)
    }
}
