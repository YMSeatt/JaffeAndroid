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

    @Serializable
    data class MoveItems(
        override val description: String,
        val moves: List<MementoItemMove>
    ) : MementoCommand()
}

@Serializable
data class MementoItemMove(
    val id: Long,
    val itemType: String, // "STUDENT" or "FURNITURE"
    val oldX: Float,
    val oldY: Float,
    val newX: Float,
    val newY: Float,
    val student: Student? = null,
    val furniture: Furniture? = null
)
