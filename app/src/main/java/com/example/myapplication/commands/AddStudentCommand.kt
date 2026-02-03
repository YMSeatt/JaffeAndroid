package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to add a new student to the seating chart.
 * Reversing this command removes the student from the database.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param student The [Student] entity to be added.
 */
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
