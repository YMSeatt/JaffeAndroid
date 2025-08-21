package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.data.StudentGroupDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentGroupsViewModel(private val studentGroupDao: StudentGroupDao) : ViewModel() {

    val allStudentGroups: StateFlow<List<StudentGroup>> = studentGroupDao.getAllStudentGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addGroup(groupName: String, groupColor: String) { // Modified to take name and color
        viewModelScope.launch {
            studentGroupDao.insert(StudentGroup(name = groupName, color = groupColor))
        }
    }

    fun updateGroup(group: StudentGroup) {
        viewModelScope.launch {
            studentGroupDao.update(group)
        }
    }

    fun deleteGroup(group: StudentGroup) {
        viewModelScope.launch {
            studentGroupDao.delete(group)
        }
    }
}


