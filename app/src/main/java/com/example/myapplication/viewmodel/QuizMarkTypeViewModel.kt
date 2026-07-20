package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.QuizMarkTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * QuizMarkTypeViewModel: Manages the customization of quiz grading criteria and point scales.
 *
 * This Hilt-powered ViewModel coordinates the CRUD lifecycle of [QuizMarkType] entities. These types
 * govern how individual student answers are weighted when calculating quiz scores and overall class performance.
 *
 * ### Architectural Roles:
 * - **Unidirectional Data Flow (UDF)**: Exposes the list of current mark types reactively to Compose screens,
 *   supporting standard 5-second caching window via [SharingStarted.WhileSubscribed].
 * - **Initialization Safety**: Includes a [resetToDefaults] helper to restore standard, validated grading scales,
 *   safeguarding against accidental deletion of critical grading categories.
 *
 * @property repository Repository providing thread-safe operations on the underlying SQLite `quiz_mark_types` table.
 */
@HiltViewModel
class QuizMarkTypeViewModel @Inject constructor(
    private val repository: QuizMarkTypeRepository
) : ViewModel() {

    /**
     * Reactive, read-only [StateFlow] stream containing all registered quiz mark types.
     * Observed by the mark management list and template builder screens.
     */
    val quizMarkTypes = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Asynchronously inserts a new grading mark type.
     *
     * @param quizMarkType The [QuizMarkType] configuration (name, point weight, contribution flags) to insert.
     */
    fun insert(quizMarkType: QuizMarkType) = viewModelScope.launch {
        repository.insert(quizMarkType)
    }

    /**
     * Asynchronously saves updates to an existing grading mark type.
     *
     * @param quizMarkType The [QuizMarkType] entity with updated details.
     */
    fun update(quizMarkType: QuizMarkType) = viewModelScope.launch {
        repository.update(quizMarkType)
    }

    /**
     * Asynchronously deletes a grading mark type.
     *
     * @param quizMarkType The [QuizMarkType] entity to remove.
     */
    fun delete(quizMarkType: QuizMarkType) = viewModelScope.launch {
        repository.delete(quizMarkType)
    }

    /**
     * Resets the quiz mark configuration to standard school defaults.
     *
     * This operation performs a safe, bulk-replacement in the database:
     * 1. **Deletes all** existing custom mark types to clear the slate.
     * 2. **Inserts standard scales**:
     *    - **"Correct"**: Base answer worth `1.0` point (contributes to the score denominator).
     *    - **"Incorrect"**: Base wrong answer worth `0.0` points (contributes to the score denominator).
     *    - **"Partial Credit"**: Partial correctness worth `0.5` points (contributes to the score denominator).
     *    - **"Bonus"**: Extra credit worth `1.0` point (does **NOT** add to the score denominator, allowing scores > 100%).
     */
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
