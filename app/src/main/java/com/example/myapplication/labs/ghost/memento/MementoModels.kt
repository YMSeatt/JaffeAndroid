package com.example.myapplication.labs.ghost.memento

import com.example.myapplication.data.*
import kotlinx.serialization.Serializable

/**
 * MementoHistory: A serializable container for the application's command history.
 *
 * This structure allows the [com.example.myapplication.viewmodel.SeatingChartViewModel]'s
 * undo and redo stacks to be persisted to disk, enabling history recovery across app restarts.
 *
 * @property undoStack A list of commands that can be undone, ordered from oldest to newest.
 * @property redoStack A list of commands that can be redone.
 */
@Serializable
data class MementoHistory(
    val undoStack: List<MementoCommand> = emptyList(),
    val redoStack: List<MementoCommand> = emptyList()
)

/**
 * MementoCommand: A serializable representation of a reversible application action.
 *
 * This sealed class mirrors the production [com.example.myapplication.commands.Command]
 * hierarchy but stores only the raw data needed to reconstruct the command objects.
 */
@Serializable
sealed class MementoCommand {
    abstract val description: String

    @Serializable
    data class AddStudent(
        override val description: String,
        val student: Student
    ) : MementoCommand()

    @Serializable
    data class DeleteStudent(
        override val description: String,
        val student: Student
    ) : MementoCommand()

    @Serializable
    data class UpdateStudent(
        override val description: String,
        val oldStudent: Student,
        val newStudent: Student
    ) : MementoCommand()

    @Serializable
    data class MoveStudent(
        override val description: String,
        val studentId: Int,
        val oldX: Float,
        val oldY: Float,
        val newX: Float,
        val newY: Float
    ) : MementoCommand()

    @Serializable
    data class LogBehavior(
        override val description: String,
        val event: BehaviorEvent
    ) : MementoCommand()

    @Serializable
    data class AddFurniture(
        override val description: String,
        val furniture: Furniture
    ) : MementoCommand()

    @Serializable
    data class DeleteFurniture(
        override val description: String,
        val furniture: Furniture
    ) : MementoCommand()

    @Serializable
    data class UpdateFurniture(
        override val description: String,
        val oldFurniture: Furniture,
        val newFurniture: Furniture
    ) : MementoCommand()

    @Serializable
    data class MoveFurniture(
        override val description: String,
        val furnitureId: Int,
        val oldX: Float,
        val oldY: Float,
        val newX: Float,
        val newY: Float
    ) : MementoCommand()

    @Serializable
    data class AddGuide(
        override val description: String,
        val guide: Guide
    ) : MementoCommand()

    @Serializable
    data class DeleteGuide(
        override val description: String,
        val guide: Guide
    ) : MementoCommand()

    @Serializable
    data class MoveGuide(
        override val description: String,
        val guide: Guide,
        val oldPosition: Float,
        val newPosition: Float
    ) : MementoCommand()

    @Serializable
    data class LogHomework(
        override val description: String,
        val log: HomeworkLog
    ) : MementoCommand()

    @Serializable
    data class LogQuiz(
        override val description: String,
        val log: QuizLog
    ) : MementoCommand()

    @Serializable
    data class LoadLayout(
        override val description: String,
        val layout: LayoutTemplate,
        val oldStudents: List<Student>,
        val oldFurniture: List<Furniture>
    ) : MementoCommand()

    @Serializable
    data class Composite(
        override val description: String,
        val commands: List<MementoCommand>
    ) : MementoCommand()

    /**
     * Bulk movement command for multiple students and/or furniture items.
     */
    @Serializable
    data class MoveItems(
        override val description: String,
        val moves: List<MementoItemMove>
    ) : MementoCommand()
}

/**
 * MementoItemMove: Captures the transformation of a single seating chart item.
 *
 * This DTO ensures that bulk move operations can be perfectly reconstructed.
 * It stores full snapshots of the [student] or [furniture] entity to maintain
 * referential integrity if the move is redone after the original item has
 * been modified by other commands.
 *
 * @property id The database primary key of the item.
 * @property itemType Discriminator string: "STUDENT" or "FURNITURE".
 * @property oldX Original horizontal coordinate.
 * @property oldY Original vertical coordinate.
 * @property newX Target horizontal coordinate.
 * @property newY Target vertical coordinate.
 * @property student Complete snapshot of the student (null if itemType is "FURNITURE").
 * @property furniture Complete snapshot of the furniture (null if itemType is "STUDENT").
 */
@Serializable
data class MementoItemMove(
    val id: Long,
    val itemType: String,
    val oldX: Float,
    val oldY: Float,
    val newX: Float,
    val newY: Float,
    val student: Student? = null,
    val furniture: Furniture? = null
)
