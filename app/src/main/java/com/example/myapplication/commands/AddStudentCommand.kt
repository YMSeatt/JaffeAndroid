package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

class AddStudentCommand(
    private val viewModel: SeatingChartViewModel,
    private val student: Student
) : Command {
    override suspend fun execute() {
        viewModel.internalAddStudent(student)
    }

    override suspend fun undo() {
        viewModel.internalDeleteStudent(student)
    }

    override fun getDescription(): String = "Add student: ${student.firstName} ${student.lastName}"
}
