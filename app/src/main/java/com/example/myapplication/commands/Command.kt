package com.example.myapplication.commands

/**
 * Represents a reversible operation within the application.
 * The Command pattern is used to support a robust Undo/Redo system,
 * allowing users to revert changes to the seating chart layout, student data, and logs.
 */
interface Command {
    /**
     * Executes the command, applying the intended change to the application state.
     * This usually involves calling a corresponding "internal" method in the ViewModel.
     */
    suspend fun execute()

    /**
     * Reverses the effect of the command, restoring the application state to what it was
     * before [execute] was called.
     */
    suspend fun undo()

    /**
     * Provides a human-readable description of the action performed by this command.
     * This is used for displaying action history in the UI (e.g., in the Undo History dialog).
     *
     * @return A localized or descriptive string (e.g., "Move Student: John Doe").
     */
    fun getDescription(): String
}
