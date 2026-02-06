package com.example.myapplication.util

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Student
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CollisionDetectorTest {

    private fun createStudentUiItem(
        id: Int,
        x: Float,
        y: Float,
        width: Float = 150f,
        height: Float = 100f
    ): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = mutableStateOf("Student $id"),
            nickname = mutableStateOf(null),
            initials = mutableStateOf("S$id"),
            xPosition = mutableStateOf(x),
            yPosition = mutableStateOf(y),
            displayWidth = mutableStateOf(width.dp),
            displayHeight = mutableStateOf(height.dp),
            displayBackgroundColor = mutableStateOf(listOf(Color.White)),
            displayOutlineColor = mutableStateOf(listOf(Color.Black)),
            displayTextColor = mutableStateOf(Color.Black),
            displayOutlineThickness = mutableStateOf(1.dp),
            displayCornerRadius = mutableStateOf(0.dp),
            displayPadding = mutableStateOf(0.dp),
            fontFamily = mutableStateOf("Arial"),
            fontSize = mutableStateOf(12),
            fontColor = mutableStateOf(Color.Black),
            recentBehaviorDescription = mutableStateOf(emptyList()),
            recentHomeworkDescription = mutableStateOf(emptyList()),
            recentQuizDescription = mutableStateOf(emptyList()),
            groupColor = mutableStateOf(null),
            groupId = mutableStateOf(null),
            sessionLogText = mutableStateOf(emptyList()),
            temporaryTask = mutableStateOf(null)
        )
    }

    private fun createStudent(id: Long, width: Int? = null, height: Int? = null): Student {
        return Student(
            id = id,
            firstName = "Student",
            lastName = "$id",
            stringId = "$id",
            customWidth = width,
            customHeight = height
        )
    }

    @Test
    fun `resolveCollisions returns (0,0) for zero canvas width`() {
        val movedStudent = createStudent(1)
        val students = emptyList<StudentUiItem>()
        val (x, y) = CollisionDetector.resolveCollisions(movedStudent, students, 0, 1000)
        assertEquals(0f, x, 0.01f)
        assertEquals(0f, y, 0.01f)
    }

    @Test
    fun `places first student in top-left of grid`() {
        val movedStudent = createStudent(1, width = 150, height = 100)
        val students = emptyList<StudentUiItem>()
        // Canvas = 800 wide. Icon = 150 + 10 padding = 160. 800 / 160 = 5 columns.
        // Column width = 800 / 5 = 160.
        // X pos = col * colWidth + (colWidth - iconWidth) / 2
        // X pos = 0 * 160 + (160 - 150) / 2 = 5f
        val (x, y) = CollisionDetector.resolveCollisions(movedStudent, students, 800, 1000)
        assertEquals(5f, x, 0.01f)
        assertEquals(0f, y, 0.01f)
    }

    @Test
    fun `places second student in next available spot (top-down, left-to-right)`() {
        val movedStudent = createStudent(2, width = 150, height = 100)
        // Student 1 is at (5, 0)
        val s1 = createStudentUiItem(1, 5f, 0f, width = 150f, height = 100f)
        val students = listOf(s1)
        // Canvas = 800 wide. 5 columns.
        // Algorithm should find the next open spot at the top of the canvas, which is column 1, y=0
        val (x, y) = CollisionDetector.resolveCollisions(movedStudent, students, 800, 1000)
        assertEquals(165f, x, 0.01f) // Column 1
        assertEquals(0f, y, 0.01f)
    }

    @Test
    fun `places student in new column if first is full`() {
        val movedStudent = createStudent(6, width = 150, height = 100)
        val students = listOf(
            createStudentUiItem(1, 5f, 0f),
            createStudentUiItem(2, 165f, 0f),
            createStudentUiItem(3, 325f, 0f),
            createStudentUiItem(4, 485f, 0f),
            createStudentUiItem(5, 645f, 0f)
        )
        // All top row spots are filled. Should go to the next available spot, which is Col 0, Row 1
        // Y pos = 0 (y of s1) + 100 (height of s1) + 10 (padding) = 110
        val (x, y) = CollisionDetector.resolveCollisions(movedStudent, students, 800, 1000)
        assertEquals(5f, x, 0.01f) // Column 0
        assertEquals(110f, y, 0.01f)
    }

     @Test
    fun `finds empty spot in grid`() {
        val movedStudent = createStudent(4, width = 150, height = 100)
        // Grid with a hole in the second column
        // Col 0
        val s1 = createStudentUiItem(1, 5f, 0f)
        val s2 = createStudentUiItem(2, 5f, 110f)
        // Col 1 - s3 is missing at y=0
        // Col 2
        val s3 = createStudentUiItem(3, 325f, 0f)
        val students = listOf(s1, s2, s3)

        // It should find the empty spot in column 1 at y=0
        // Col 1 X pos = 165
        val (x, y) = CollisionDetector.resolveCollisions(movedStudent, students, 800, 1000)
        assertEquals(165f, x, 0.01f)
        assertEquals(0f, y, 0.01f)
    }
}
