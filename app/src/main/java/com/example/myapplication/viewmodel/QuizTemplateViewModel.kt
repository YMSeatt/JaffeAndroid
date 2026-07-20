package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.QuizTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * QuizTemplateViewModel: Handles operations and reactive data streams for custom quiz structures.
 *
 * This Hilt-powered ViewModel manages the state of the Quiz Template Builder interface. It exposes
 * a complete catalog of available quiz templates and current quiz mark types (e.g. Correct, Incorrect, Partial).
 *
 * ### Architectural Roles:
 * - **State Synchronization**: Exposes database streams via [StateFlow]s configured with [SharingStarted.WhileSubscribed]
 *   to ensure that UI changes are reactive and resources are released when the template settings are closed.
 * - **Modular Configuration**: Integrates with [QuizTemplateRepository] (for template cataloging) and
 *   [com.example.myapplication.data.QuizMarkTypeRepository] (for selecting which types of marks can be mapped
 *   to a quiz).
 *
 * @property repository Repository for managing [QuizTemplate] database records.
 * @property markTypeRepository Repository for fetching the available mark types used within quiz builders.
 */
@HiltViewModel
class QuizTemplateViewModel @Inject constructor(
    private val repository: QuizTemplateRepository,
    private val markTypeRepository: com.example.myapplication.data.QuizMarkTypeRepository
) : ViewModel() {

    /**
     * Reactive, read-only [StateFlow] containing all defined quiz templates.
     * Keeps the template selection UI in sync with the database, releasing handles 5 seconds after all subscribers disconnect.
     */
    val quizTemplates = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Reactive, read-only [StateFlow] containing all available mark types (e.g., "Correct", "Half Credit").
     * Used by the template creator to let users assign default points and scoring logic.
     */
    val quizMarkTypes = markTypeRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Asynchronously inserts a new quiz template definition into the local database.
     *
     * @param quizTemplate The new [QuizTemplate] to save.
     */
    fun insert(quizTemplate: QuizTemplate) = viewModelScope.launch {
        repository.insert(quizTemplate)
    }

    /**
     * Asynchronously updates fields (e.g., name, question counts, default marks) of an existing quiz template.
     *
     * @param quizTemplate The updated [QuizTemplate] record.
     */
    fun update(quizTemplate: QuizTemplate) = viewModelScope.launch {
        repository.update(quizTemplate)
    }

    /**
     * Asynchronously deletes a quiz template.
     *
     * @param quizTemplate The [QuizTemplate] record to delete.
     */
    fun delete(quizTemplate: QuizTemplate) = viewModelScope.launch {
        repository.delete(quizTemplate)
    }
}
