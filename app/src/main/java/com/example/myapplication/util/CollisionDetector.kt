package com.example.myapplication.util

import com.example.myapplication.data.Student
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.max

/**
 * Utility for detecting and resolving spatial collisions on the seating chart.
 *
 * This detector uses a greedy, column-based placement algorithm to find the first
 * available empty space for a student icon. It prioritizes filling from the top-down,
 * then left-to-right, ensuring a compact and organized layout.
 */
object CollisionDetector {

    /** Default padding between student icons in pixels. */
    private const val PADDING = 10
    /** Default width of a student icon if not specified in [Student]. */
    private const val DEFAULT_WIDTH = 150
    /** Default height of a student icon if not specified in [Student]. */
    private const val DEFAULT_HEIGHT = 100

    /**
     * Finds the next best available position for a student on the canvas that does not overlap
     * with existing students.
     *
     * The algorithm follows these steps:
     * 1. **Grid Assignment**: Divides the canvas into columns based on student icon width.
     * 2. **Column Mapping**: Assigns existing students to these columns based on their current X position.
     * 3. **Gap Searching**: For each column, it looks for the first vertical gap large enough to fit
     *    the new student icon plus padding.
     * 4. **Optimal Selection**: Collects all potential spots across all columns and selects the one
     *    with the lowest Y coordinate (highest on screen). If Y coordinates are tied, it picks the
     *    lowest X coordinate (leftmost).
     *
     * @param movedStudent The [Student] entity being placed or moved.
     * @param students The list of [StudentUiItem]s already present on the chart.
     * @param canvasWidth The total width of the seating chart canvas.
     * @param canvasHeight The total height of the seating chart canvas. If <= 0, vertical bounds are ignored.
     * @return A [Pair] containing the (X, Y) coordinates of the chosen spot.
     */
    fun resolveCollisions(
        movedStudent: Student,
        students: List<StudentUiItem>,
        canvasWidth: Int,
        canvasHeight: Int
    ): Pair<Float, Float> {
        if (canvasWidth == 0) return Pair(0f, 0f)

        val iconWidth = movedStudent.customWidth ?: DEFAULT_WIDTH
        val iconHeight = movedStudent.customHeight ?: DEFAULT_HEIGHT

        // Step 1: Divide canvas into columns
        val numColumns = max(1, canvasWidth / (iconWidth + PADDING))
        val columnWidth = canvasWidth / numColumns

        val grid = mutableMapOf<Int, MutableList<StudentUiItem>>()

        // Step 2: Map existing students to columns
        for (student in students) {
            if (student.id.toLong() == movedStudent.id) continue
            val studentX = max(0f, student.xPosition.value)
            val col = (studentX / columnWidth).toInt().coerceIn(0, numColumns - 1)
            grid.getOrPut(col) { mutableListOf() }.add(student)
        }

        val potentialSpots = mutableListOf<Pair<Float, Float>>()

        // Step 3: Find the first available spot in each column
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
                // Check if there's enough room before this student
                if (currentY + iconHeight + PADDING < student.yPosition.value) {
                    potentialSpots.add(Pair(x, currentY))
                    spotFoundInCol = true
                    break
                }
                // Move currentY to the bottom of this student plus padding
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

        // Step 4: Find the best spot: Min Y, then Min X
        return spotsToSearch.minWithOrNull(compareBy({ it.second }, { it.first })) ?: Pair(0f, 0f)
    }
}
