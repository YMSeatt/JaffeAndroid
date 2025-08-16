package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

class AddStudentCommand(
    private val viewModel: SeatingChartViewModel,
    private val student: Student
) : Command {
    override fun execute() {
        viewModel.internalAddStudent(student)
    }

    override fun undo() {
        viewModel.deleteStudent(student)
    }
}
