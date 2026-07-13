package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.data.StudentGroupDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * StudentGroupsViewModel: Manages the relational grouping of students.
 *
 * Student groups allow teachers to organize their classroom for collaborative
 * work and specialized seating arrangements. Group membership is used by:
 * 1. **Neural Map Layer**: To draw connection lines between group members.
 * 2. **Cognitive Engine**: To apply attraction forces, keeping groups physically close.
 * 3. **Conditional Formatting**: To apply styles based on group ID.
 */
@HiltViewModel
class StudentGroupsViewModel @Inject constructor(private val studentGroupDao: StudentGroupDao) : ViewModel() {

    /** Reactive stream of all groups, used by the Group Editor and selection dialogs. */
    val allStudentGroups: StateFlow<List<StudentGroup>> = studentGroupDao.getAllStudentGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Persists a new student group.
     */
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


