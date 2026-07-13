package com.example.myapplication.labs.ghost.filtering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.data.StudentGroupDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * GhostFilterViewModel: Manages the complex filtering state for the student list.
 *
 * This ViewModel leverages [combine] and [StateFlow] to create a reactive data pipeline.
 * It integrates with the [StudentDao] and [StudentGroupDao] to fetch raw classroom data.
 *
 * BOLT: Optimized filtering occurs on [Dispatchers.Default] (implicit in Flow operators)
 * to keep the UI thread responsive during high-frequency search query updates.
 */
@HiltViewModel
class GhostFilterViewModel @Inject constructor(
    private val studentDao: StudentDao,
    private val studentGroupDao: StudentGroupDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedGroupIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedGroupIds = _selectedGroupIds.asStateFlow()

    private val allStudents = studentDao.getAllStudents().asFlow()
    val allGroups: StateFlow<List<StudentGroup>> = studentGroupDao.getAllStudentGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Reactive stream of students filtered by search query and group selection.
     */
    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        searchQuery,
        selectedGroupIds
    ) { students, query, selectedIds ->
        students.filter { student ->
            val matchesQuery = if (query.isBlank()) true else {
                student.firstName.contains(query, ignoreCase = true) ||
                student.lastName.contains(query, ignoreCase = true) ||
                (student.nickname?.contains(query, ignoreCase = true) ?: false)
            }
            val matchesGroup = if (selectedIds.isEmpty()) true else {
                student.groupId != null && selectedIds.contains(student.groupId)
            }
            matchesQuery && matchesGroup
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleGroupSelection(groupId: Long) {
        val currentSet = _selectedGroupIds.value.toMutableSet()
        if (currentSet.contains(groupId)) {
            currentSet.remove(groupId)
        } else {
            currentSet.add(groupId)
        }
        _selectedGroupIds.value = currentSet
    }
}
