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
    private val repository: QuizTemplateRepository,
    private val markTypeRepository: com.example.myapplication.data.QuizMarkTypeRepository
) : ViewModel() {

    val quizTemplates = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quizMarkTypes = markTypeRepository.getAll()
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
}
