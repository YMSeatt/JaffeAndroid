package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizMarkTypeRepository @Inject constructor(
    private val quizMarkTypeDao: QuizMarkTypeDao
) {
    fun getAll(): Flow<List<QuizMarkType>> = quizMarkTypeDao.getAllQuizMarkTypes()

    suspend fun insert(quizMarkType: QuizMarkType) {
        quizMarkTypeDao.insert(quizMarkType)
    }

    suspend fun update(quizMarkType: QuizMarkType) {
        quizMarkTypeDao.update(quizMarkType)
    }

    suspend fun delete(quizMarkType: QuizMarkType) {
        quizMarkTypeDao.delete(quizMarkType)
    }

    suspend fun getAllList(): List<QuizMarkType> = quizMarkTypeDao.getAllQuizMarkTypesList()

    suspend fun insertAll(markTypes: List<QuizMarkType>) {
        markTypes.forEach { quizMarkTypeDao.insert(it) }
    }

    suspend fun deleteAll() {
        quizMarkTypeDao.deleteAll()
    }
}
