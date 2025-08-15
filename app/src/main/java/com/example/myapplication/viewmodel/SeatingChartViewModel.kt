package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.switchMap // Added for switchMap

// Add these imports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp // For .dp extension
import com.example.myapplication.ui.model.StudentUiItem // Our new UI model

import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.Student
// StudentDetailsForDisplay is still needed for the source LiveData from repository
import com.example.myapplication.data.StudentDetailsForDisplay 
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_BG_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_HEIGHT_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_WIDTH_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP // New import
import kotlinx.coroutines.launch

// Helper function to safely parse color strings
private fun safeParseColor(colorString: String?, defaultColor: Color): Color {
    return try {
        colorString?.let { Color(android.graphics.Color.parseColor(it)) } ?: defaultColor
    } catch (e: IllegalArgumentException) {
        defaultColor // Fallback to default if parsing fails
    }
}

class SeatingChartViewModel(application: Application) : AndroidViewModel(application) {

    private val appPreferencesRepository: AppPreferencesRepository = AppPreferencesRepository(application)
    private val studentRepository: StudentRepository

    // Changed to LiveData<List<StudentUiItem>>
    val studentsForDisplay: LiveData<List<StudentUiItem>>

    // LiveData for the preference of recent incidents limit
    val recentIncidentsLimit: LiveData<Int> = appPreferencesRepository.recentBehaviorIncidentsLimitFlow.asLiveData()

    init {
        val studentDao = AppDatabase.getDatabase(application).studentDao()
        val behaviorEventDao = AppDatabase.getDatabase(application).behaviorEventDao()
        val homeworkLogDao = AppDatabase.getDatabase(application).homeworkLogDao()
        studentRepository = StudentRepository(studentDao, behaviorEventDao, homeworkLogDao)

        // The MediatorLiveData will now produce List<StudentUiItem>
        val mediator = MediatorLiveData<List<StudentUiItem>>()

        val defaultWidthLD = appPreferencesRepository.defaultStudentBoxWidthFlow.asLiveData()
        val defaultHeightLD = appPreferencesRepository.defaultStudentBoxHeightFlow.asLiveData()
        val defaultBgColorLD = appPreferencesRepository.defaultStudentBoxBackgroundColorFlow.asLiveData()
        val defaultOutlineColorLD = appPreferencesRepository.defaultStudentBoxOutlineColorFlow.asLiveData()
        val defaultTextColorLD = appPreferencesRepository.defaultStudentBoxTextColorFlow.asLiveData()
        val defaultOutlineThicknessLD = appPreferencesRepository.defaultStudentBoxOutlineThicknessFlow.asLiveData() // New LiveData source

        val sourceStudentsDetailsLd = studentRepository.studentsForDisplay // This is LiveData<List<StudentDetailsForDisplay>>

        val updateMediator = { ->
            val studentsFromRepo = sourceStudentsDetailsLd.value
            val currentDefaultWidth = defaultWidthLD.value ?: DEFAULT_STUDENT_BOX_WIDTH_DP
            val currentDefaultHeight = defaultHeightLD.value ?: DEFAULT_STUDENT_BOX_HEIGHT_DP
            val currentDefaultBgColorHex = defaultBgColorLD.value ?: DEFAULT_STUDENT_BOX_BG_COLOR_HEX
            val currentDefaultOutlineColorHex = defaultOutlineColorLD.value ?: DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
            val currentDefaultTextColorHex = defaultTextColorLD.value ?: DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
            val currentDefaultOutlineThickness = defaultOutlineThicknessLD.value ?: DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP // New value retrieval

            // Define default Color objects once
            val defaultBgComposeColor = safeParseColor(currentDefaultBgColorHex, Color.White)
            val defaultOutlineComposeColor = safeParseColor(currentDefaultOutlineColorHex, Color.Black)
            val defaultTextComposeColor = safeParseColor(currentDefaultTextColorHex, Color.Black)

            if (studentsFromRepo != null) {
                val studentUiItemList = studentsFromRepo.map { studentDbDetails ->
                    val displayWidthDp = (studentDbDetails.customWidth ?: currentDefaultWidth).dp
                    val displayHeightDp = (studentDbDetails.customHeight ?: currentDefaultHeight).dp

                    val displayBgColor = safeParseColor(studentDbDetails.customBackgroundColor, defaultBgComposeColor)
                    val displayOutlineColor = safeParseColor(studentDbDetails.customOutlineColor, defaultOutlineComposeColor)
                    val displayTextColor = safeParseColor(studentDbDetails.customTextColor, defaultTextComposeColor)

                    StudentUiItem(
                        id = studentDbDetails.id,
                        fullName = "${studentDbDetails.firstName} ${studentDbDetails.lastName}",
                        initials = studentDbDetails.getEffectiveInitials(),
                        xPosition = studentDbDetails.xPosition,
                        yPosition = studentDbDetails.yPosition,
                        displayWidth = displayWidthDp,
                        displayHeight = displayHeightDp,
                        displayBackgroundColor = displayBgColor,
                        displayOutlineColor = displayOutlineColor,
                        displayTextColor = displayTextColor,
                        displayOutlineThickness = currentDefaultOutlineThickness.dp, // Using the preference value
                        recentBehaviorDescription = studentDbDetails.recentBehaviorDescription
                    )
                }
                mediator.postValue(studentUiItemList)
            } else {
                 mediator.postValue(emptyList())
            }
        }

        mediator.addSource(sourceStudentsDetailsLd) { updateMediator() }
        mediator.addSource(defaultWidthLD) { updateMediator() }
        mediator.addSource(defaultHeightLD) { updateMediator() }
        mediator.addSource(defaultBgColorLD) { updateMediator() }
        mediator.addSource(defaultOutlineColorLD) { updateMediator() }
        mediator.addSource(defaultTextColorLD) { updateMediator() }
        mediator.addSource(defaultOutlineThicknessLD) { updateMediator() } // Add as source

        studentsForDisplay = mediator
    }

    fun getAllStudentsForExport(): List<Student>? {
        return studentRepository.allStudents.value
    }

    fun addStudent(student: Student) = viewModelScope.launch {
        // Ensure initials are set if not provided
        if (student.initials.isNullOrBlank()) {
            student.initials = student.getGeneratedInitials()
        }
        studentRepository.insertStudent(student)
    }

    fun updateStudent(student: Student) = viewModelScope.launch {
        // Ensure initials are set if not provided (e.g., if they were cleared)
        if (student.initials.isNullOrBlank()) {
            student.initials = student.getGeneratedInitials()
        }
        studentRepository.updateStudent(student)
    }

    fun deleteStudent(student: Student) = viewModelScope.launch {
        studentRepository.deleteStudent(student)
    }

    fun updateStudentPosition(studentId: Int, newX: Float, newY: Float) = viewModelScope.launch {
        val student = studentRepository.getStudentByIdNonLiveData(studentId)
        student?.let {
            it.xPosition = newX.toDouble() // Ensure type consistency for xPosition
            it.yPosition = newY.toDouble() // Ensure type consistency for yPosition
            // Ensure initials are preserved or regenerated if necessary during an update
            if (it.initials.isNullOrBlank()) {
                 it.initials = it.getGeneratedInitials()
            }
            studentRepository.updateStudent(it)
        } ?: run {
            // Handle student not found
        }
    }

    // Suspend function to get a student by ID for editing purposes
    suspend fun getStudentForEditing(studentId: Int): Student? {
        return studentRepository.getStudentByIdNonLiveData(studentId)
    }

    fun addBehaviorEvent(event: BehaviorEvent) = viewModelScope.launch {
        studentRepository.insertBehaviorEvent(event)
    }

    fun getAllBehaviorEvents(): LiveData<List<BehaviorEvent>> {
        return studentRepository.getAllBehaviorEvents()
    }

    // Function to get recent behavior events for a student, observing the limit from preferences
    fun getRecentBehaviorEventsForStudentWithLimit(studentId: Long): LiveData<List<BehaviorEvent>> {
        return appPreferencesRepository.recentBehaviorIncidentsLimitFlow.asLiveData().switchMap { limit ->
            studentRepository.getRecentBehaviorEventsForStudent(studentId, limit)
        }
    }

    fun addHomeworkLog(homeworkLog: HomeworkLog) = viewModelScope.launch {
        studentRepository.insertHomeworkLog(homeworkLog)
    }

    fun getHomeworkLogsForStudent(studentId: Long): LiveData<List<HomeworkLog>> {
        return studentRepository.getHomeworkLogsForStudent(studentId)
    }

    fun getAllHomeworkLogs(): LiveData<List<HomeworkLog>> {
        return studentRepository.getAllHomeworkLogs()
    }

    fun deleteHomeworkLog(logId: Long) = viewModelScope.launch {
        studentRepository.deleteHomeworkLog(logId)
    }

    fun getRecentHomeworkLogsForStudent(studentId: Long, limit: Int): LiveData<List<HomeworkLog>> {
        return studentRepository.getRecentHomeworkLogsForStudent(studentId, limit)
    }
}
