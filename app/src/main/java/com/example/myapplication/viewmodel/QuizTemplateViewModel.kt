package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.QuizTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizTemplateViewModel @Inject constructor(
    private val repository: QuizTemplateRepository
) : ViewModel() {

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    val quizTemplates = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTemplate(quizTemplate: QuizTemplate): Boolean {
        val currentTemplates = quizTemplates.value
        if (currentTemplates.any { it.name.equals(quizTemplate.name, ignoreCase = true) }) {
            _errorFlow.value = "A quiz template named '${quizTemplate.name}' already exists."
            return false
        }
        viewModelScope.launch {
            repository.insert(quizTemplate)
        }
        return true
    }

    fun updateTemplate(quizTemplate: QuizTemplate): Boolean {
        val currentTemplates = quizTemplates.value
        if (currentTemplates.any { it.id != quizTemplate.id && it.name.equals(quizTemplate.name, ignoreCase = true) }) {
            _errorFlow.value = "Another quiz template named '${quizTemplate.name}' already exists."
            return false
        }
        viewModelScope.launch {
            repository.update(quizTemplate)
        }
        return true
    }

    fun delete(quizTemplate: QuizTemplate) = viewModelScope.launch {
        repository.delete(quizTemplate)
    }

    fun clearError() {
        _errorFlow.value = null
    }
}
