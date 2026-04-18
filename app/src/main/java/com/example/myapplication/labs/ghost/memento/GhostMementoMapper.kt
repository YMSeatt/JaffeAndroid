package com.example.myapplication.labs.ghost.memento

import com.example.myapplication.commands.*
import com.example.myapplication.viewmodel.SeatingChartViewModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GhostMementoMapper: Bridge between production Commands and serializable Mementos.
 *
 * This utility handles the bi-directional mapping required for the "Ghost Memento"
 * feature. It decomposes complex [Command] objects into pure-data [MementoCommand]s
 * for storage, and reconstructs live [Command] instances from persisted data.
 */
@Singleton
class GhostMementoMapper @Inject constructor() {

    /**
     * Maps a production [Command] to its serializable [MementoCommand] counterpart.
     * Uses exhaustive type checking to ensure all command types are handled.
     */
    fun toMemento(command: Command): MementoCommand? {
        return when (command) {
            is AddStudentCommand -> MementoCommand.AddStudent(command.getDescription(), command.student)
            is DeleteStudentCommand -> MementoCommand.DeleteStudent(command.getDescription(), command.student)
            is UpdateStudentCommand -> MementoCommand.UpdateStudent(command.getDescription(), command.oldStudent, command.newStudent)
            is MoveStudentCommand -> MementoCommand.MoveStudent(command.getDescription(), command.studentId, command.oldX, command.oldY, command.newX, command.newY)
            is LogBehaviorCommand -> MementoCommand.LogBehavior(command.getDescription(), command.event)
            is AddFurnitureCommand -> MementoCommand.AddFurniture(command.getDescription(), command.furniture)
            is DeleteFurnitureCommand -> MementoCommand.DeleteFurniture(command.getDescription(), command.furniture)
            is UpdateFurnitureCommand -> MementoCommand.UpdateFurniture(command.getDescription(), command.oldFurniture, command.newFurniture)
            is MoveFurnitureCommand -> MementoCommand.MoveFurniture(command.getDescription(), command.furnitureId, command.oldX, command.oldY, command.newX, command.newY)
            is AddGuideCommand -> MementoCommand.AddGuide(command.getDescription(), command.guide)
            is DeleteGuideCommand -> MementoCommand.DeleteGuide(command.getDescription(), command.guide)
            is MoveGuideCommand -> MementoCommand.MoveGuide(command.getDescription(), command.guide, command.oldPosition, command.newPosition)
            is LogHomeworkCommand -> MementoCommand.LogHomework(command.getDescription(), command.log)
            is LogQuizCommand -> MementoCommand.LogQuiz(command.getDescription(), command.log)
            is LoadLayoutCommand -> MementoCommand.LoadLayout(command.getDescription(), command.layout, command.oldStudents, command.oldFurniture)
            is CompositeCommand -> MementoCommand.Composite(command.getDescription(), command.commands.mapNotNull { toMemento(it) })
            is MoveItemsCommand -> MementoCommand.MoveItems(
                command.getDescription(),
                command.moves.map {
                    MementoItemMove(it.id, it.itemType.name, it.oldX, it.oldY, it.newX, it.newY, it.student, it.furniture)
                }
            )
            else -> null
        }
    }

    /**
     * Reconstructs a production [Command] from a persisted [MementoCommand].
     * Requires a reference to the [SeatingChartViewModel] to wire up database dependencies.
     */
    fun fromMemento(memento: MementoCommand, viewModel: SeatingChartViewModel): Command? {
        return when (memento) {
            is MementoCommand.AddStudent -> AddStudentCommand(viewModel, memento.student)
            is MementoCommand.DeleteStudent -> DeleteStudentCommand(viewModel, memento.student)
            is MementoCommand.UpdateStudent -> UpdateStudentCommand(viewModel, memento.oldStudent, memento.newStudent)
            is MementoCommand.MoveStudent -> MoveStudentCommand(viewModel, memento.studentId, memento.oldX, memento.oldY, memento.newX, memento.newY)
            is MementoCommand.LogBehavior -> LogBehaviorCommand(viewModel, memento.event)
            is MementoCommand.AddFurniture -> AddFurnitureCommand(viewModel, memento.furniture)
            is MementoCommand.DeleteFurniture -> DeleteFurnitureCommand(viewModel, memento.furniture)
            is MementoCommand.UpdateFurniture -> UpdateFurnitureCommand(viewModel, memento.oldFurniture, memento.newFurniture)
            is MementoCommand.MoveFurniture -> MoveFurnitureCommand(viewModel, memento.furnitureId, memento.oldX, memento.oldY, memento.newX, memento.newY)
            is MementoCommand.AddGuide -> AddGuideCommand(viewModel, memento.guide)
            is MementoCommand.DeleteGuide -> DeleteGuideCommand(viewModel, memento.guide)
            is MementoCommand.MoveGuide -> MoveGuideCommand(viewModel, memento.guide, memento.oldPosition, memento.newPosition)
            is MementoCommand.LogHomework -> LogHomeworkCommand(viewModel, memento.log)
            is MementoCommand.LogQuiz -> LogQuizCommand(viewModel, memento.log)
            is MementoCommand.LoadLayout -> LoadLayoutCommand(viewModel, memento.layout, memento.oldStudents, memento.oldFurniture)
            is MementoCommand.Composite -> CompositeCommand(memento.commands.mapNotNull { fromMemento(it, viewModel) }, memento.description)
            is MementoCommand.MoveItems -> MoveItemsCommand(
                viewModel,
                memento.moves.map {
                    ItemMove(it.id, ItemType.valueOf(it.itemType), it.oldX, it.oldY, it.newX, it.newY, it.student, it.furniture)
                }
            )
        }
    }
}
