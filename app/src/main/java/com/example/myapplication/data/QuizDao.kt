package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    // QuizTemplate operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizTemplate(template: QuizTemplate)

    @Update
    suspend fun updateQuizTemplate(template: QuizTemplate)

    @Delete
    suspend fun deleteQuizTemplate(template: QuizTemplate)

    @Query("SELECT * FROM quiz_templates")
    fun getAllQuizTemplates(): Flow<List<QuizTemplate>>

    // Quiz operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz)

    @Update
    suspend fun updateQuiz(quiz: Quiz)

    @Delete
    suspend fun deleteQuiz(quiz: Quiz)

    @Query("SELECT * FROM quizzes WHERE student_id = :studentId")
    fun getQuizzesForStudent(studentId: Long): Flow<List<Quiz>>
}
