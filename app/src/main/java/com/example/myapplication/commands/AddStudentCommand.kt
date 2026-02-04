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
    private var generatedId: Long = student.id

    override suspend fun execute() {
        val studentToInsert = if (generatedId != 0L) student.copy(id = generatedId) else student
        generatedId = viewModel.internalAddStudent(studentToInsert)
    }

    override suspend fun undo() {
        viewModel.internalDeleteStudent(student.copy(id = generatedId))
    }

    override fun getDescription(): String = "Add student: ${student.firstName} ${student.lastName}"
}
