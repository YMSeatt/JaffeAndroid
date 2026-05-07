package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Normalized Quiz subsystem.
 *
 * This DAO manages the "Source of Truth" for standardized assessments. It maintains
 * the lifecycle of both [QuizTemplate] blueprints and the individual student [Quiz]
 * attempts.
 *
 * ### Comparison: QuizDao vs. QuizLogDao
 * - **[QuizLogDao]**: Manages legacy, self-contained logs. Best for rapid entry and
 *   maintaining compatibility with older data formats.
 * - **[QuizDao]**: Manages relational assessment data. Preferred for generating
 *   consistent longitudinal reports based on standardized templates.
 */
@Dao
interface QuizDao {
    // --- QuizTemplate operations ---

    /**
     * Persists a new quiz blueprint.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizTemplate(template: QuizTemplate)

    /**
     * Updates an existing quiz blueprint.
     */
    @Update
    suspend fun updateQuizTemplate(template: QuizTemplate)

    /**
     * Removes a quiz blueprint from the database.
     */
    @Delete
    suspend fun deleteQuizTemplate(template: QuizTemplate)

    /**
     * Returns a reactive stream of all available quiz templates.
     */
    @Query("SELECT * FROM quiz_templates")
    fun getAllQuizTemplates(): Flow<List<QuizTemplate>>

    // --- Quiz operations ---

    /**
     * Persists an individual student's quiz attempt.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz)

    /**
     * Updates an existing student quiz record.
     */
    @Update
    suspend fun updateQuiz(quiz: Quiz)

    /**
     * Removes a quiz attempt from history.
     */
    @Delete
    suspend fun deleteQuiz(quiz: Quiz)

    /**
     * Returns a reactive stream of all quiz attempts for a specific student.
     */
    @Query("SELECT * FROM quizzes WHERE student_id = :studentId")
    fun getQuizzesForStudent(studentId: Long): Flow<List<Quiz>>
}
