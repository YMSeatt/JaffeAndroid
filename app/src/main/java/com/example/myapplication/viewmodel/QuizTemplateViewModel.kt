package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.QuizMarkType
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

    val defaultMarkTypes = listOf(
        QuizMarkType(id = 1, name = "Correct", defaultPoints = 1.0, contributesToTotal = true, isExtraCredit = false),
        QuizMarkType(id = 2, name = "Incorrect", defaultPoints = 0.0, contributesToTotal = true, isExtraCredit = false),
        QuizMarkType(id = 3, name = "Partial Credit", defaultPoints = 0.5, contributesToTotal = true, isExtraCredit = false),
        QuizMarkType(id = 4, name = "Bonus", defaultPoints = 1.0, contributesToTotal = false, isExtraCredit = true)
    )

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
}
