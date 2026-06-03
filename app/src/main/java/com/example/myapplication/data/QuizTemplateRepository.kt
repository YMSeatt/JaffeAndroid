package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository facilitating access to quiz template data.
 *
 * This class coordinates the retrieval and modification of [QuizTemplate] entities,
 * acting as an abstraction layer over the [QuizTemplateDao]. It ensures that
 * academic assessment definitions are managed consistently across the application.
 */
class QuizTemplateRepository @Inject constructor(
    private val quizTemplateDao: QuizTemplateDao
) {
    /**
     * Retrieves an observable stream of all available quiz templates.
     */
    fun getAll(): Flow<List<QuizTemplate>> = quizTemplateDao.getAll()

    /**
     * Persists a new template definition.
     */
    suspend fun insert(quizTemplate: QuizTemplate) {
        quizTemplateDao.insert(quizTemplate)
    }

    /**
     * Updates an existing template.
     */
    suspend fun update(quizTemplate: QuizTemplate) {
        quizTemplateDao.update(quizTemplate)
    }

    /**
     * Deletes a template from the database.
     */
    suspend fun delete(quizTemplate: QuizTemplate) {
        quizTemplateDao.delete(quizTemplate)
    }
}
