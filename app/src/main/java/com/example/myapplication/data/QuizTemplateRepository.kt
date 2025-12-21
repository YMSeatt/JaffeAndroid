package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuizTemplateRepository @Inject constructor(
    private val quizTemplateDao: QuizTemplateDao
) {
    fun getAll(): Flow<List<QuizTemplate>> = quizTemplateDao.getAll()

    suspend fun insert(quizTemplate: QuizTemplate) {
        quizTemplateDao.insert(quizTemplate)
    }

    suspend fun update(quizTemplate: QuizTemplate) {
        quizTemplateDao.update(quizTemplate)
    }

    suspend fun delete(quizTemplate: QuizTemplate) {
        quizTemplateDao.delete(quizTemplate)
    }
}
