package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for quiz templates.
 *
 * Provides persistence methods for [QuizTemplate] entities, which define standardized
 * academic assessment structures (e.g., number of questions, default point values).
 */
@Dao
interface QuizTemplateDao {
    /**
     * Observes all saved templates, sorted by name.
     */
    @Query("SELECT * FROM quiz_templates ORDER BY name ASC")
    fun getAll(): Flow<List<QuizTemplate>>

    /**
     * Creates or updates a quiz template.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quizTemplate: QuizTemplate)

    /**
     * Updates an existing template's configuration.
     */
    @Update
    suspend fun update(quizTemplate: QuizTemplate)

    /**
     * Deletes a template from the library.
     */
    @Delete
    suspend fun delete(quizTemplate: QuizTemplate)
}
