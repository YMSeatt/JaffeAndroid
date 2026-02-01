package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.QuizMarkTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizMarkTypeViewModel @Inject constructor(
    private val repository: QuizMarkTypeRepository
) : ViewModel() {

    val quizMarkTypes = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(quizMarkType: QuizMarkType) = viewModelScope.launch {
        repository.insert(quizMarkType)
    }

    fun update(quizMarkType: QuizMarkType) = viewModelScope.launch {
        repository.update(quizMarkType)
    }

    fun delete(quizMarkType: QuizMarkType) = viewModelScope.launch {
        repository.delete(quizMarkType)
    }

    fun resetToDefaults() = viewModelScope.launch {
        val defaults = listOf(
            QuizMarkType(name = "Correct", defaultPoints = 1.0, contributesToTotal = true, isExtraCredit = false),
            QuizMarkType(name = "Incorrect", defaultPoints = 0.0, contributesToTotal = true, isExtraCredit = false),
            QuizMarkType(name = "Partial Credit", defaultPoints = 0.5, contributesToTotal = true, isExtraCredit = false),
            QuizMarkType(name = "Bonus", defaultPoints = 1.0, contributesToTotal = false, isExtraCredit = true)
        )
        repository.deleteAll()
        repository.insertAll(defaults)
    }
}
