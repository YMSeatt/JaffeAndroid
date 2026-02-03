package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to update a student's information or styling.
 * Stores a copy of both the old and new student states to allow reverting changes.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param oldStudent The student state before the update.
 * @param newStudent The student state after the update.
 */
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

    override fun getDescription(): String = "Update student: ${newStudent.firstName} ${newStudent.lastName}"
}
