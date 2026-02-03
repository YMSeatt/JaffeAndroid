package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to remove a student from the seating chart.
 * Reversing this command re-inserts the student back into the database.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param student The [Student] entity to be deleted.
 */
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

    override fun getDescription(): String = "Delete student: ${student.firstName} ${student.lastName}"
}
