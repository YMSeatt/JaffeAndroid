package com.example.myapplication.commands

/**
 * A command that groups multiple other commands into a single atomic action.
 * Executing this command executes all sub-commands in order.
 * Undoing this command undoes all sub-commands in reverse order.
 *
 * @param commands The list of commands to group.
 * @param description A human-readable description of the bulk action.
 */
class CompositeCommand(
    private val commands: List<Command>,
    private val description: String
) : Command {
    override suspend fun execute() {
        commands.forEach { it.execute() }
    }

    override suspend fun undo() {
        // Undo in reverse order to maintain state consistency
        commands.reversed().forEach { it.undo() }
    }

    override fun getDescription(): String = description
}
