package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.StudentRepository
import kotlinx.coroutines.launch

class HomeworkLogViewModel(application: Application, private val studentId: Long) : AndroidViewModel(application) {

    private val studentRepository: StudentRepository

    // LiveData to hold the list of homework logs for the current studentId
    val homeworkLogsForStudent: LiveData<List<HomeworkLog>>

    // Potentially a LiveData for a single selected log if we add edit functionality
    // private val _selectedHomeworkLog = MutableLiveData<HomeworkLog?>()
    // val selectedHomeworkLog: LiveData<HomeworkLog?> = _selectedHomeworkLog

    init {
        val studentDao = AppDatabase.getDatabase(application).studentDao()
        val behaviorEventDao = AppDatabase.getDatabase(application).behaviorEventDao()
        val homeworkLogDao = AppDatabase.getDatabase(application).homeworkLogDao()
        studentRepository = StudentRepository(studentDao, behaviorEventDao, homeworkLogDao)

        // Initialize homeworkLogsForStudent based on studentId
        // This requires studentId to be available at init.
        // If studentId can change, this setup needs to be more dynamic.
        homeworkLogsForStudent = studentRepository.getHomeworkLogsForStudent(studentId)
    }

    /**
     * Adds a new homework log for the current student.
     */
    fun addHomeworkLog(assignmentName: String, status: String, comment: String?) {
        viewModelScope.launch {
            val newLog = HomeworkLog(
                studentId = studentId,
                assignmentName = assignmentName,
                status = status,
                comment = comment,
                loggedAt = System.currentTimeMillis() // Or allow setting a custom date
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

    // If we need to load a specific log for viewing/editing:
    // fun loadHomeworkLogById(logId: Long) {
    //     viewModelScope.launch {
    //         // Assuming a repository method getHomeworkLogById(logId) exists
    //         // _selectedHomeworkLog.value = studentRepository.getHomeworkLogById(logId) 
    //     }
    // }
}

// We might need a ViewModelProvider.Factory if the ViewModel has constructor parameters
// that the default factory doesn't handle (like studentId here).
// For simple cases with just Application and other known types, it might work,
// but for `studentId: Long`, a factory is typically needed.

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
