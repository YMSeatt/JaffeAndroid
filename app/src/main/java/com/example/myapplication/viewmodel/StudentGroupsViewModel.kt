package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.data.StudentGroupDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private val StudentGroupsViewModel.viewModelScope: Any

class StudentGroupsViewModel(private val studentGroupDao: StudentGroupDao) : ViewModel() {

    val studentGroups: StateFlow<List<StudentGroup>> = studentGroupDao.getAllStudentGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addGroup(groupName: String, groupColor: String) { // Modified to take name and color
        viewModelScope.launch {
            studentGroupDao.insertStudentGroup(StudentGroup(name = groupName, color = groupColor))
        }
    }

    fun updateGroup(group: StudentGroup) {
        viewModelScope.launch {
            studentGroupDao.updateStudentGroup(group)
        }
    }

    fun deleteGroup(group: StudentGroup) {
        viewModelScope.launch {
            studentGroupDao.deleteStudentGroup(group)
        }
    }
}

private fun Any.launch(block: suspend (CoroutineScope) -> Unit) {
    TODO("Not yet implemented")
}
