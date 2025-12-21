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
        width: Float = 100f,
        height: Float = 100f
    ): StudentUiItem {
        return StudentUiItem(
            id = id,
            fullName = "Student $id",
            nickname = null,
            initials = "S$id",
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
            recentBehaviorDescription = emptyList(),
            recentHomeworkDescription = emptyList(),
            recentQuizDescription = emptyList(),
            groupColor = null,
            groupId = null,
            temporaryTask = null
        )
    }

    private fun createStudent(id: Long): Student {
        return Student(
            id = id,
            firstName = "Student",
            lastName = "$id",
            stringId = "$id"
        )
    }

    @Test
    fun `resolveCollisions returns (0,0) for empty list`() {
        val movedStudent = createStudent(1)
        val students = emptyList<StudentUiItem>()
        val result = CollisionDetector.resolveCollisions(movedStudent, students, 1000)
        assertEquals(0f, result.first, 0.01f)
        assertEquals(0f, result.second, 0.01f)
    }

    @Test
    fun `resolveCollisions places second student below first if fits`() {
        val s1 = createStudentUiItem(1, 0f, 0f, 100f, 100f)
        val movedStudent = createStudent(2)
        val students = listOf(s1)
        
        // s1 is in column 0. columnWidths[0] = 100.
        // resolveCollisions loop for i=0:
        // currentY starts at 0.
        // s1.y is 0. 0 + 100 < 0 is false.
        // currentY becomes 0 + 100 + 10 = 110.
        // canvasHeight is 1000. 110 + 100 < 1000 is true.
        // returns (0, 110).
        
        val result = CollisionDetector.resolveCollisions(movedStudent, students, 1000)
        assertEquals(0f, result.first, 0.01f)
        assertEquals(110f, result.second, 0.01f)
    }

    @Test
    fun `resolveCollisions places student in new column if height exceeded`() {
        val s1 = createStudentUiItem(1, 0f, 0f, 100f, 100f)
        // Canvas height 150. s1 takes 100. Next needs 100. 110 + 100 = 210 > 150.
        // Should move to next column.
        
        val movedStudent = createStudent(2)
        val students = listOf(s1)
        
        val result = CollisionDetector.resolveCollisions(movedStudent, students, 150)
        
        // Column 0 width = 100. Padding = 10.
        // Next X should be 100 + 10 = 110.
        assertEquals(110f, result.first, 0.01f)
        assertEquals(0f, result.second, 0.01f)
    }

    @Test
    fun `resolveCollisions finds gap between students`() {
        val s1 = createStudentUiItem(1, 0f, 0f, 100f, 100f)
        val s2 = createStudentUiItem(2, 0f, 300f, 100f, 100f)
        val movedStudent = createStudent(3)
        val students = listOf(s1, s2)
        
        // Column 0 has s1 at 0 and s2 at 300.
        // i=0, student=s1: currentY=0. 0+100 < 0 false. currentY = 0+100+10 = 110.
        // i=0, student=s2: currentY=110. 110+100 < 300? Yes (210 < 300).
        // Returns (0, 110).
        
        val result = CollisionDetector.resolveCollisions(movedStudent, students, 1000)
        assertEquals(0f, result.first, 0.01f)
        assertEquals(110f, result.second, 0.01f)
    }
}
