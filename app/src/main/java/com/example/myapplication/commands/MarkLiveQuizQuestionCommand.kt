package com.example.myapplication.commands

import com.example.myapplication.data.QuizLog
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to record a student's response during an active live quiz session.
 * Reversing this command restores the previous quiz score state.
 *
 * @param viewModel The ViewModel to perform the operations.
 * @param quizLog The [QuizLog] entity representing the student's answer.
 */
class MarkLiveQuizQuestionCommand(
    internal val viewModel: SeatingChartViewModel,
    internal val quizLog: QuizLog
) : Command {
    private var previousScoreState: Map<String, Any>? = null
    private var wasSavedToDb: Boolean = false
    internal var generatedId: Long = quizLog.id

    override suspend fun execute() {
        if (viewModel.isSessionActive.value == true) {
            // Store the previous score state for the student before applying changes
            previousScoreState = viewModel.liveQuizScores.value?.get(quizLog.studentId)

            val logToInsert = if (generatedId != 0L) quizLog.copy(id = generatedId) else quizLog
            // Add to session logs list
            val currentLogs = viewModel.getSessionQuizLogs().toMutableList()
            currentLogs.add(logToInsert)
            viewModel.setSessionQuizLogs(currentLogs)

            // Update live scores map
            val allScores = viewModel.liveQuizScores.value?.toMutableMap() ?: mutableMapOf()
            val studentScores = allScores[quizLog.studentId]?.toMutableMap() ?: mutableMapOf()
            studentScores["last_response"] = quizLog.comment ?: ""
            studentScores["mark_value"] = quizLog.markValue ?: 0.0
            studentScores["max_mark_value"] = quizLog.maxMarkValue ?: 0.0
            studentScores["marks_data"] = quizLog.marksData

            if (quizLog.numQuestions > 0) {
                val totalAsked = (studentScores["total_asked"] as? Int ?: 0) + 1
                studentScores["total_asked"] = totalAsked
            }

            if (quizLog.comment.equals("Correct", ignoreCase = true)) {
                val correctCount = (studentScores["correct"] as? Int ?: 0) + 1
                studentScores["correct"] = correctCount
            }

            allScores[quizLog.studentId] = studentScores
            viewModel.setLiveQuizScores(allScores)
            wasSavedToDb = false
        } else {
            // Save directly to database
            val logToInsert = if (generatedId != 0L) quizLog.copy(id = generatedId) else quizLog
            generatedId = viewModel.internalSaveQuizLog(logToInsert)
            wasSavedToDb = true
        }
    }

    override suspend fun undo() {
        if (wasSavedToDb) {
            viewModel.deleteQuizLog(quizLog.copy(id = generatedId))
        } else {
            // Remove the appended log from sessionQuizLogs
            val currentLogs = viewModel.getSessionQuizLogs().toMutableList()
            if (currentLogs.isNotEmpty()) {
                val index = currentLogs.indexOfLast { it.studentId == quizLog.studentId && it.quizName == quizLog.quizName }
                if (index != -1) {
                    currentLogs.removeAt(index)
                }
            }
            viewModel.setSessionQuizLogs(currentLogs)

            // Restore the previous score state
            val allScores = viewModel.liveQuizScores.value?.toMutableMap() ?: mutableMapOf()
            if (previousScoreState != null) {
                allScores[quizLog.studentId] = previousScoreState!!
            } else {
                allScores.remove(quizLog.studentId)
            }
            viewModel.setLiveQuizScores(allScores)
        }
    }

    override fun getDescription(): String {
        val student = viewModel.allStudents.value?.find { it.id == quizLog.studentId }
        val name = student?.let { "${it.firstName} ${it.lastName}" } ?: "student"
        return "Mark Quiz: ${quizLog.comment ?: "Answer"} for $name"
    }
}
