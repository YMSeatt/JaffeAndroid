package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.StudentRepository
import kotlinx.coroutines.launch

class HomeworkLogViewModel(application: Application, private val studentId: Long) : AndroidViewModel(application) {
class HomeworkLogViewModel(application: Application, private val studentId: Long) : AndroidViewModel(application) {

    private val studentRepository: StudentRepository

    // LiveData to hold the list of homework logs for the current studentId
    val homeworkLogsForStudent: LiveData<List<HomeworkLog>>

    init {
        val studentDao = AppDatabase.getDatabase(application).studentDao()
        val behaviorEventDao = AppDatabase.getDatabase(application).behaviorEventDao()
        val homeworkLogDao = AppDatabase.getDatabase(application).homeworkLogDao()
        val furnitureDao = AppDatabase.getDatabase(application).furnitureDao()
        val quizLogDao = AppDatabase.getDatabase(application).quizLogDao()
        val studentGroupDao = AppDatabase.getDatabase(application).studentGroupDao()
        val layoutTemplateDao = AppDatabase.getDatabase(application).layoutTemplateDao()
        val conditionalFormattingRuleDao = AppDatabase.getDatabase(application).conditionalFormattingRuleDao()
        val quizMarkTypeDao = AppDatabase.getDatabase(application).quizMarkTypeDao()

        studentRepository = StudentRepository(
            studentDao,
            behaviorEventDao,
            homeworkLogDao,
            furnitureDao,
            quizLogDao,
            studentGroupDao,
            layoutTemplateDao,
            conditionalFormattingRuleDao,
            quizMarkTypeDao
        )

        homeworkLogsForStudent = studentRepository.getHomeworkLogsForStudent(studentId)
    }

    /**
     * Adds a new homework log for the current student.
     */
    fun addHomeworkLog(assignmentName: String, status: String, comment: String?) {
        viewModelScope.launch {
            val newLog = HomeworkLog(
                studentId = studentId.toInt(),
                assignmentName = assignmentName,
                status = status,
                comment = comment,
                loggedAt = System.currentTimeMillis()
            )
            studentRepository.insertHomeworkLog(newLog)
        }
    }

    /**
     * Deletes a specific homework log.
     */
    fun deleteHomeworkLog(logId: Long) {
        viewModelScope.launch {
            studentRepository.deleteHomeworkLog(logId)
        }
    }
}

class HomeworkLogViewModelFactory(
    private val application: Application,
    private val studentId: Long
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeworkLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeworkLogViewModel(application, studentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}