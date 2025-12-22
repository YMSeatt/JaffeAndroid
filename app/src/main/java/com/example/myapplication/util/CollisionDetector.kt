package com.example.myapplication.util

import com.example.myapplication.data.Student
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.max

object CollisionDetector {

    private const val PADDING = 10
    private const val DEFAULT_WIDTH = 150
    private const val DEFAULT_HEIGHT = 100

    fun resolveCollisions(
        movedStudent: Student,
        students: List<StudentUiItem>,
        canvasWidth: Int,
        canvasHeight: Int
    ): Pair<Float, Float> {
        if (canvasWidth == 0) return Pair(0f, 0f)

        val iconWidth = movedStudent.customWidth ?: DEFAULT_WIDTH
        val iconHeight = movedStudent.customHeight ?: DEFAULT_HEIGHT

        val numColumns = max(1, canvasWidth / (iconWidth + PADDING))
        val columnWidth = canvasWidth / numColumns

        val grid = mutableMapOf<Int, MutableList<StudentUiItem>>()

        for (student in students) {
            if (student.id.toLong() == movedStudent.id) continue
            val studentX = max(0f, student.xPosition.value)
            val col = (studentX / columnWidth).toInt().coerceIn(0, numColumns - 1)
            grid.getOrPut(col) { mutableListOf() }.add(student)
        }

        val potentialSpots = mutableListOf<Pair<Float, Float>>()

        // Find the first available spot in each column
        for (col in 0 until numColumns) {
            val x = col * columnWidth + (columnWidth - iconWidth) / 2f
            val columnStudents = grid[col]?.sortedBy { it.yPosition.value } ?: emptyList()

            var currentY = 0f
            var spotFoundInCol = false

            if (columnStudents.isEmpty()) {
                potentialSpots.add(Pair(x, currentY))
                continue
            }

            for (student in columnStudents) {
                if (currentY + iconHeight + PADDING < student.yPosition.value) {
                    potentialSpots.add(Pair(x, currentY))
                    spotFoundInCol = true
                    break
                }
                currentY = student.yPosition.value + student.displayHeight.value.value + PADDING
            }

            if (!spotFoundInCol) {
                potentialSpots.add(Pair(x, currentY))
            }
        }

        // Filter out spots that are off-canvas, if canvasHeight is specified
        val validSpots = if (canvasHeight <= 0) {
            potentialSpots
        } else {
            potentialSpots.filter { it.second + iconHeight <= canvasHeight }
        }

        val spotsToSearch = if (validSpots.isNotEmpty()) validSpots else potentialSpots

        // Find the best spot: Min Y, then Min X
        return spotsToSearch.minWithOrNull(compareBy({ it.second }, { it.first })) ?: Pair(0f, 0f)
    }
}
