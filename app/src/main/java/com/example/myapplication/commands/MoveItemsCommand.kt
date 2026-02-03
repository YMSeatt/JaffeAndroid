package com.example.myapplication.commands

import com.example.myapplication.data.Furniture
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Represents the movement of a single item (Student or Furniture) on the seating chart.
 * Captures both the old and new positions to support undo/redo.
 *
 * @property id The ID of the item.
 * @property itemType The type of the item (STUDENT or FURNITURE).
 * @property oldX The horizontal position before the move.
 * @property oldY The vertical position before the move.
 * @property newX The horizontal position after the move.
 * @property newY The vertical position after the move.
 * @property student The full Student object (required for STUDENT type to perform bulk updates).
 * @property furniture The full Furniture object (required for FURNITURE type to perform bulk updates).
 */
data class ItemMove(
    val id: Long,
    val itemType: ItemType,
    val oldX: Float,
    val oldY: Float,
    val newX: Float,
    val newY: Float,
    val student: Student? = null,
    val furniture: Furniture? = null
)

/**
 * The type of item being moved.
 */
enum class ItemType { STUDENT, FURNITURE }

/**
 * Command to move multiple items (students and/or furniture) in a single action.
 * This ensures that bulk operations like alignment or distribution are treated as
 * a single undoable step, maintaining logical parity with the Python implementation.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param moves A list of [ItemMove] objects describing the changes to be applied.
 */
class MoveItemsCommand(
    private val viewModel: SeatingChartViewModel,
    private val moves: List<ItemMove>
) : Command {
    override suspend fun execute() {
        val studentUpdates = moves.filter { it.itemType == ItemType.STUDENT }
            .mapNotNull { it.student?.copy(xPosition = it.newX, yPosition = it.newY) }
        val furnitureUpdates = moves.filter { it.itemType == ItemType.FURNITURE }
            .mapNotNull { it.furniture?.copy(xPosition = it.newX, yPosition = it.newY) }

        viewModel.internalUpdateAll(studentUpdates, furnitureUpdates)
    }

    override suspend fun undo() {
        val studentUpdates = moves.filter { it.itemType == ItemType.STUDENT }
            .mapNotNull { it.student?.copy(xPosition = it.oldX, yPosition = it.oldY) }
        val furnitureUpdates = moves.filter { it.itemType == ItemType.FURNITURE }
            .mapNotNull { it.furniture?.copy(xPosition = it.oldX, yPosition = it.oldY) }

        viewModel.internalUpdateAll(studentUpdates, furnitureUpdates)
    }

    override fun getDescription(): String = "Move ${moves.size} item(s)"
}
