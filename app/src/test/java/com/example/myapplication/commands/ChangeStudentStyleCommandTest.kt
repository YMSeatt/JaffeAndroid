package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows

class ChangeStudentStyleCommandTest {

    private val viewModel = mockk<SeatingChartViewModel>(relaxed = true)

    @Test
    fun `execute applies custom background color style and undo restores oldValue`() = runTest {
        val studentId = 1L
        val originalStudent = Student(id = studentId, firstName = "Alice", lastName = "Smith", customBackgroundColor = "#FFFFFF")

        coEvery { viewModel.getStudentForEditing(studentId) } returns originalStudent

        val command = ChangeStudentStyleCommand(
            viewModel = viewModel,
            studentId = studentId,
            styleProperty = "customBackgroundColor",
            oldValue = "#FFFFFF",
            newValue = "#00FF00"
        )

        // Execute the style change
        command.execute()

        // Verify the student object was updated with the new background color
        val expectedNewStudent = originalStudent.copy(customBackgroundColor = "#00FF00")
        coVerify { viewModel.internalUpdateStudent(expectedNewStudent) }

        // Execute undo
        command.undo()

        // Verify the student object was restored to original background color
        coVerify { viewModel.internalUpdateStudent(originalStudent) }
    }

    @Test
    fun `execute handles null and alternate style property names for dimensions`() = runTest {
        val studentId = 2L
        val originalStudent = Student(id = studentId, firstName = "Bob", lastName = "Jones", customWidth = 100, customHeight = 80)

        coEvery { viewModel.getStudentForEditing(studentId) } returns originalStudent

        val widthCommand = ChangeStudentStyleCommand(
            viewModel = viewModel,
            studentId = studentId,
            styleProperty = "width",
            oldValue = 100,
            newValue = 150
        )

        widthCommand.execute()
        val expectedWidthStudent = originalStudent.copy(customWidth = 150)
        coVerify { viewModel.internalUpdateStudent(expectedWidthStudent) }

        val heightCommand = ChangeStudentStyleCommand(
            viewModel = viewModel,
            studentId = studentId,
            styleProperty = "height",
            oldValue = 80,
            newValue = null
        )

        heightCommand.execute()
        val expectedHeightStudent = originalStudent.copy(customHeight = null)
        coVerify { viewModel.internalUpdateStudent(expectedHeightStudent) }
    }

    @Test
    fun `execute throws exception for unknown property name`() = runTest {
        val studentId = 3L
        val originalStudent = Student(id = studentId, firstName = "Charlie", lastName = "Brown")
        coEvery { viewModel.getStudentForEditing(studentId) } returns originalStudent

        val command = ChangeStudentStyleCommand(
            viewModel = viewModel,
            studentId = studentId,
            styleProperty = "invalidProperty",
            oldValue = null,
            newValue = "someValue"
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                command.execute()
            }
        }
    }
}
