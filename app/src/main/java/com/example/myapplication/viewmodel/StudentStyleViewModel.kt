package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Student
import com.example.myapplication.data.DefaultStudentStyle
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.preferences.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentStyleViewModel @Inject constructor(
    private val repository: StudentRepository,
    preferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()

    val defaultStudentStyle: StateFlow<DefaultStudentStyle> = preferencesRepository.defaultStudentStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultStudentStyle(
            backgroundColor = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_BG_COLOR_HEX,
            outlineColor = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX,
            textColor = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX,
            width = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_WIDTH_DP,
            height = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_HEIGHT_DP,
            outlineThickness = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP,
            fontFamily = com.example.myapplication.preferences.DEFAULT_STUDENT_FONT_FAMILY,
            fontSize = com.example.myapplication.preferences.DEFAULT_STUDENT_FONT_SIZE_SP,
            fontColor = com.example.myapplication.preferences.DEFAULT_STUDENT_FONT_COLOR_HEX,
            cornerRadius = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP,
            padding = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_PADDING_DP
        ))

    fun loadStudent(studentId: Long) {
        viewModelScope.launch {
            _student.value = repository.getStudentById(studentId)
        }
    }

    fun updateStudent(seatingChartViewModel: SeatingChartViewModel, student: Student) {
        viewModelScope.launch { // Launch a coroutine for the update and subsequent load
            seatingChartViewModel.updateStudentStyle(student)
            // After the update, explicitly reload the student to ensure the StateFlow is refreshed
            _student.value = repository.getStudentById(student.id)
        }
    }
}
