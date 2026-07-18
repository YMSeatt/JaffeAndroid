package com.example.myapplication.commands

import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.util.HomeworkScoreEngine
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to record student completion status during an active live homework session.
 * Reversing this command restores the previous homework score state.
 *
 * @param viewModel The ViewModel to perform the operations.
 * @param homeworkLogs The list of [HomeworkLog] entities representing the homework status.
 */
class MarkLiveHomeworkCommand(
    internal val viewModel: SeatingChartViewModel,
    internal val homeworkLogs: List<HomeworkLog>
) : Command {
    // Map of studentId to their previous score state
    private val previousScoreStates = mutableMapOf<Long, Map<String, Any>?>()
    private var wasSavedToDb = false
    // Stores generated ids if we save directly to the database
    private val generatedIds = mutableListOf<Long>()

    override suspend fun execute() {
        if (viewModel.isSessionActive.value == true) {
            val currentLogs = viewModel.getSessionHomeworkLogs().toMutableList()
            val allLiveScores = viewModel.liveHomeworkScores.value?.toMutableMap() ?: mutableMapOf()

            homeworkLogs.forEach { homeworkLog ->
                // Store previous score state before first modification
                if (!previousScoreStates.containsKey(homeworkLog.studentId)) {
                    previousScoreStates[homeworkLog.studentId] = viewModel.liveHomeworkScores.value?.get(homeworkLog.studentId)
                }

                currentLogs.add(homeworkLog)

                val studentScores = allLiveScores[homeworkLog.studentId]?.toMutableMap() ?: mutableMapOf()
                val marksData = HomeworkScoreEngine.parseMarksData(homeworkLog.marksData)
                if (marksData.containsKey("selected_options")) {
                    studentScores["selected_options"] = marksData["selected_options"] ?: emptyList<String>()
                } else if (homeworkLog.assignmentName == "Yes/No Update") {
                    marksData.forEach { (key, value) ->
                        studentScores[key] = value
                    }
                } else {
                    studentScores[homeworkLog.assignmentName] = homeworkLog.status
                }
                allLiveScores[homeworkLog.studentId] = studentScores
            }

            viewModel.setSessionHomeworkLogs(currentLogs)
            viewModel.setLiveHomeworkScores(allLiveScores)
            wasSavedToDb = false
        } else {
            // Save directly to database
            generatedIds.clear()
            homeworkLogs.forEach { log ->
                val id = viewModel.internalAddHomeworkLog(log)
                generatedIds.add(id)
            }
            wasSavedToDb = true
        }
    }

    override suspend fun undo() {
        if (wasSavedToDb) {
            homeworkLogs.zip(generatedIds).forEach { (log, id) ->
                viewModel.deleteHomeworkLog(log.copy(id = id))
            }
        } else {
            // Remove matching logs from sessionHomeworkLogs
            val currentLogs = viewModel.getSessionHomeworkLogs().toMutableList()
            homeworkLogs.forEach { log ->
                val index = currentLogs.indexOfLast { it.studentId == log.studentId && it.assignmentName == log.assignmentName }
                if (index != -1) {
                    currentLogs.removeAt(index)
                }
            }
            viewModel.setSessionHomeworkLogs(currentLogs)

            // Restore the previous score states
            val allLiveScores = viewModel.liveHomeworkScores.value?.toMutableMap() ?: mutableMapOf()
            previousScoreStates.forEach { (studentId, previousState) ->
                if (previousState != null) {
                    allLiveScores[studentId] = previousState
                } else {
                    allLiveScores.remove(studentId)
                }
            }
            viewModel.setLiveHomeworkScores(allLiveScores)
        }
    }

    override fun getDescription(): String {
        val studentNames = homeworkLogs.mapNotNull { log ->
            viewModel.allStudents.value?.find { it.id == log.studentId }
        }.distinctBy { it.id }.joinToString { "${it.firstName} ${it.lastName}" }

        val nameDisplay = if (studentNames.isNotEmpty()) studentNames else "student(s)"
        return "Mark HW: for $nameDisplay"
    }
}
