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

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError = _validationError.asStateFlow()

    val quizTemplates = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(quizTemplate: QuizTemplate) = viewModelScope.launch {
        val nameExists = quizTemplates.value.any {
            it.name.equals(quizTemplate.name, ignoreCase = true)
        }
        if (nameExists) {
            _validationError.value = "A template with this name already exists."
        } else {
            repository.insert(quizTemplate)
        }
    }

    fun update(quizTemplate: QuizTemplate) = viewModelScope.launch {
        val nameExists = quizTemplates.value.any {
            it.name.equals(quizTemplate.name, ignoreCase = true) && it.id != quizTemplate.id
        }
        if (nameExists) {
            _validationError.value = "A template with this name already exists."
        } else {
            repository.update(quizTemplate)
        }
    }

    fun delete(quizTemplate: QuizTemplate) = viewModelScope.launch {
        repository.delete(quizTemplate)
    }

    fun clearValidationError() {
        _validationError.value = null
    }
}
