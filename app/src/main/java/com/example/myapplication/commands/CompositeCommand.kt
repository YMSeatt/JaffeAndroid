package com.example.myapplication.commands

/**
 * A command that groups multiple other commands into a single atomic action.
 *
 * This implementation of the Macro Command pattern ensures that complex operations
 * (like aligning or distributing multiple items) are treated as a single entry
 * in the user's undo history.
 *
 * - **Execution**: Sub-commands are executed in their original list order.
 * - **Undo**: Sub-commands are undone in **reverse** order to ensure state consistency,
 *   as later actions might depend on the state produced by earlier ones.
 *
 * @param commands The list of commands to group and execute atomically.
 * @param description A human-readable description of the bulk action (e.g., "Align 5 Items").
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
