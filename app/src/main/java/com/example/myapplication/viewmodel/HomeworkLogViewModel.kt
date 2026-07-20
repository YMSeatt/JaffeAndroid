package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.launch

class HomeworkLogViewModel(
    application: Application,
    private val studentId: Long,
    securityUtil: SecurityUtil
) : AndroidViewModel(application) {

    private val studentRepository: StudentRepository

    val homeworkLogsForStudent: LiveData<List<HomeworkLog>>

    init {
        val db = AppDatabase.getDatabase(application)
        studentRepository = StudentRepository(
            db.studentDao(),
            db.behaviorEventDao(),
            db.homeworkLogDao(),
            db.quizLogDao(),
            db.furnitureDao(),
            db.layoutTemplateDao(),
            db.quizMarkTypeDao(),
            application,
            securityUtil
        )

        homeworkLogsForStudent = studentRepository.getHomeworkLogsForStudent(studentId)
    }

    fun addHomeworkLog(assignmentName: String, status: String, comment: String?) {
        viewModelScope.launch {
            val newLog = HomeworkLog(
                studentId = studentId,
                assignmentName = assignmentName,
                status = status,
                comment = comment,
                loggedAt = System.currentTimeMillis()
            )
            studentRepository.insertHomeworkLog(newLog)
        }
    }

    fun deleteHomeworkLog(logId: Long) {
        viewModelScope.launch {
            studentRepository.deleteHomeworkLogById(logId)
        }
    }

    class HomeworkLogViewModelFactory(
        private val application: Application,
        private val studentId: Long,
        private val securityUtil: SecurityUtil
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeworkLogViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeworkLogViewModel(application, studentId, securityUtil) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
