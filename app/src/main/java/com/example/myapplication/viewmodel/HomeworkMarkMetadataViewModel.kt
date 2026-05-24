package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.HomeworkMarkMetadata
import com.example.myapplication.data.HomeworkMarkMetadataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeworkMarkMetadataViewModel: Manages the custom scoring categories for homework.
 *
 * This ViewModel provides the logic for adding, editing, deleting, and resetting
 * homework mark metadata (e.g., "Complete" = 10 pts).
 *
 * ### Logic Parity
 * Resets to defaults based on the Python blueprint:
 * - Complete: 10.0
 * - Incomplete: 5.0
 * - Not Done: 0.0
 * - Effort Score (1-5): 3.0
 */
@HiltViewModel
class HomeworkMarkMetadataViewModel @Inject constructor(
    private val repository: HomeworkMarkMetadataRepository
) : ViewModel() {

    val homeworkMarkMetadata = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(metadata: HomeworkMarkMetadata) = viewModelScope.launch {
        repository.insert(metadata)
    }

    fun update(metadata: HomeworkMarkMetadata) = viewModelScope.launch {
        repository.update(metadata)
    }

    fun delete(metadata: HomeworkMarkMetadata) = viewModelScope.launch {
        repository.delete(metadata)
    }

    fun resetToDefaults() = viewModelScope.launch {
        val defaults = listOf(
            HomeworkMarkMetadata(name = "Complete", defaultPoints = 10.0),
            HomeworkMarkMetadata(name = "Incomplete", defaultPoints = 5.0),
            HomeworkMarkMetadata(name = "Not Done", defaultPoints = 0.0),
            HomeworkMarkMetadata(name = "Effort Score (1-5)", defaultPoints = 3.0)
        )
        repository.replaceAll(defaults)
    }
}
