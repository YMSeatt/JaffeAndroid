package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that coordinates access to [QuizMarkType] metadata.
 *
 * This repository serves as the single source of truth for the ViewModels and scoring
 * engines regarding how quiz marks are defined.
 *
 * ### Scoring Heuristic:
 * The application's scoring logic (see `QuizScoreEngine`) relies on a naming convention
 * managed through this repository. It specifically scans for a mark type named **"Correct"**
 * (case-insensitive) to establish the base point value for the quiz denominator.
 */
@Singleton
class QuizMarkTypeRepository @Inject constructor(
    private val quizMarkTypeDao: QuizMarkTypeDao
) {
    /**
     * Returns a reactive stream of all available [QuizMarkType] definitions.
     */
    fun getAll(): Flow<List<QuizMarkType>> = quizMarkTypeDao.getAllQuizMarkTypes()

    /**
     * Persists a new mark type definition.
     */
    suspend fun insert(quizMarkType: QuizMarkType) {
        quizMarkTypeDao.insert(quizMarkType)
    }

    /**
     * Updates an existing mark type definition.
     */
    suspend fun update(quizMarkType: QuizMarkType) {
        quizMarkTypeDao.update(quizMarkType)
    }

    /**
     * Deletes a mark type definition.
     */
    suspend fun delete(quizMarkType: QuizMarkType) {
        quizMarkTypeDao.delete(quizMarkType)
    }

    /**
     * Returns a one-shot list of all [QuizMarkType] definitions.
     * Useful for initializing scoring contexts in the background update pipeline.
     */
    suspend fun getAllList(): List<QuizMarkType> = quizMarkTypeDao.getAllQuizMarkTypesList()

    /**
     * Batch inserts a list of mark type definitions.
     */
    suspend fun insertAll(markTypes: List<QuizMarkType>) {
        markTypes.forEach { quizMarkTypeDao.insert(it) }
    }

    /**
     * Removes all mark type definitions from the database.
     */
    suspend fun deleteAll() {
        quizMarkTypeDao.deleteAll()
    }
}
