package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentStyleViewModel @Inject constructor(
    private val repository: StudentRepository
) : ViewModel() {

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()

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
