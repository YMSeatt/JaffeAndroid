package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals

class AddStudentCommandTest {

    private val viewModel = mockk<SeatingChartViewModel>(relaxed = true)

    @Test
    fun `execute adds student and captures generated ID`() = runTest {
        val initialStudent = Student(id = 0, firstName = "John", lastName = "Doe")
        val generatedId = 5L
        coEvery { viewModel.internalAddStudent(any()) } returns generatedId

        val command = AddStudentCommand(viewModel, initialStudent)
        command.execute()

        coVerify { viewModel.internalAddStudent(initialStudent) }

        // After execute, undo should use the generated ID
        command.undo()
        coVerify { viewModel.internalDeleteStudent(initialStudent.copy(id = generatedId)) }
    }

    @Test
    fun `redo uses captured ID`() = runTest {
        val initialStudent = Student(id = 0, firstName = "John", lastName = "Doe")
        val generatedId = 5L
        coEvery { viewModel.internalAddStudent(any()) } returns generatedId

        val command = AddStudentCommand(viewModel, initialStudent)

        // First execution
        command.execute()
        coVerify { viewModel.internalAddStudent(initialStudent) }

        // Undo
        command.undo()
        coVerify { viewModel.internalDeleteStudent(initialStudent.copy(id = generatedId)) }

        // Redo (second execution)
        command.execute()
        // It should use the captured ID this time
        coVerify { viewModel.internalAddStudent(initialStudent.copy(id = generatedId)) }
    }
}
