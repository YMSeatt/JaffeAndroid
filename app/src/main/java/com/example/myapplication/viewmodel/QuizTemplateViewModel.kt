package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.QuizTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizTemplateViewModel @Inject constructor(
    private val repository: QuizTemplateRepository
) : ViewModel() {

    val quizTemplates = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(quizTemplate: QuizTemplate) = viewModelScope.launch {
        repository.insert(quizTemplate)
    }

    fun update(quizTemplate: QuizTemplate) = viewModelScope.launch {
        repository.update(quizTemplate)
    }

    fun delete(quizTemplate: QuizTemplate) = viewModelScope.launch {
        repository.delete(quizTemplate)
    }

    /**
     * Saves a quiz template, checking for duplicate names first.
     * @param quizTemplate The template to save.
     * @return `true` if the template was saved successfully, `false` if a duplicate name exists.
     */
    fun saveTemplate(quizTemplate: QuizTemplate): Boolean {
        val existingTemplate = quizTemplates.value.find {
            it.name.equals(quizTemplate.name, ignoreCase = true) && it.id != quizTemplate.id
        }

        if (existingTemplate != null) {
            return false // Duplicate name found
        }

        if (quizTemplate.id == 0) {
            insert(quizTemplate)
        } else {
            update(quizTemplate)
        }
        return true
    }
}
