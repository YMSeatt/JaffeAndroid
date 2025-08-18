package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

class DeleteStudentCommand(
    private val viewModel: SeatingChartViewModel,
    private val student: Student
) : Command {
    override suspend fun execute() {
        viewModel.internalDeleteStudent(student)
    }

    override suspend fun undo() {
        viewModel.internalAddStudent(student)
    }
}
