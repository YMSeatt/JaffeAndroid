package com.example.myapplication.util

import com.example.myapplication.data.Student
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.max

object CollisionDetector {

    private const val PADDING = 10
    private const val DEFAULT_HEIGHT = 100

    fun resolveCollisions(
        movedStudent: Student,
        students: List<StudentUiItem>,
        canvasHeight: Int
    ): Pair<Float, Float> {
        if (students.isEmpty() || (students.size == 1 && students[0].id.toLong() == movedStudent.id)) {
            return Pair(0f, 0f)
        }

        val columns = mutableListOf<MutableList<StudentUiItem>>()
        val columnWidths = mutableListOf<Int>()

        for (student in students) {
            if (student.id.toLong() == movedStudent.id) continue
            var placed = false
            for (i in 0 until columns.size) {
                val columnRightEdge = columnWidths.slice(0..i).sum() + PADDING * i
                if (student.xPosition.value < columnRightEdge) {
                    columns[i].add(student)
                    columnWidths[i] = max(columnWidths[i], student.displayWidth.value.value.toInt())
                    placed = true
                    break
                }
            }
            if (!placed) {
                columns.add(mutableListOf(student))
                columnWidths.add(student.displayWidth.value.value.toInt())
            }
        }

        if (columns.isEmpty()) return Pair(0f, 0f)

        for (i in 0 until columns.size) {
            val column = columns[i]
            column.sortBy { it.yPosition.value }
            var currentY: Float = 0.0f
            for (student in column) {
                if (currentY + (movedStudent.customHeight
                        ?: DEFAULT_HEIGHT) < student.yPosition.value
                ) {
                    val x = (columnWidths.slice(0 until i).sum() + PADDING * i).toFloat()
                    return Pair(x, currentY)
                }
                currentY =
                    (student.yPosition.value + student.displayHeight.value.value + PADDING).toFloat()
            }
            if (canvasHeight == 0 || currentY + (movedStudent.customHeight
                    ?: DEFAULT_HEIGHT) < canvasHeight
            ) {
                val x = (columnWidths.slice(0 until i).sum() + PADDING * i).toFloat()
                return Pair(x, currentY)
            }
        }
        // Add a new column
        val x = (columnWidths.sum() + PADDING * columns.size).toFloat()
        return Pair(x, 0f)
    }
}
