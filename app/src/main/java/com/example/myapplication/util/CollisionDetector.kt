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
        if (students.isEmpty()) {
            return Pair(0f, 0f)
        }

        val columns = mutableListOf<MutableList<StudentUiItem>>()
        val columnWidths = mutableListOf<Int>()

        // Initialize with a single column
        columns.add(mutableListOf())
        columnWidths.add(0)

        for (student in students) {
            if (student.id.toLong() == movedStudent.id) continue
            // A simple heuristic to assign students to columns, can be improved
            var placed = false
            for (i in 0 until columns.size) {
                if (student.xPosition < (columnWidths.slice(0 until i)
                        .sum() + columnWidths[i] + PADDING * i)
                ) {
                    columns[i].add(student)
                    columnWidths[i] =
                        max(columnWidths[i], student.displayWidth.value.toInt())
                    placed = true
                    break
                }
            }
            if (!placed) {
                columns.add(mutableListOf(student))
                columnWidths.add(student.displayWidth.value.toInt())
            }
        }

        for (i in 0 until columns.size) {
            val column = columns[i]
            column.sortBy { it.yPosition }
            var currentY = 0f
            for (student in column) {
                if (currentY + (movedStudent.customHeight
                        ?: DEFAULT_HEIGHT) < student.yPosition
                ) {
                    val x = (columnWidths.slice(0 until i).sum() + PADDING * i).toFloat()
                    return Pair(x, currentY)
                }
                currentY =
                    student.yPosition + student.displayHeight.value.toInt() + PADDING.toFloat()
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
