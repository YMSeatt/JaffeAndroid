package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Normalized Homework subsystem.
 *
 * This DAO manages the creation of multi-step [HomeworkTemplate] blueprints
 * and student-specific [Homework] completion records.
 */
@Dao
interface HomeworkDao {
    // --- HomeworkTemplate operations ---

    /**
     * Persists a new multi-step homework blueprint.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworkTemplate(template: HomeworkTemplate)

    /**
     * Updates an existing homework blueprint.
     */
    @Update
    suspend fun updateHomeworkTemplate(template: HomeworkTemplate)

    /**
     * Removes a homework blueprint from the database.
     */
    @Delete
    suspend fun deleteHomeworkTemplate(template: HomeworkTemplate)

    /**
     * Returns a reactive stream of all available homework templates.
     */
    @Query("SELECT * FROM homework_templates")
    fun getAllHomeworkTemplates(): Flow<List<HomeworkTemplate>>

    // --- Homework operations ---

    /**
     * Persists an individual student's homework completion record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: Homework)

    /**
     * Updates an existing homework completion record.
     */
    @Update
    suspend fun updateHomework(homework: Homework)

    /**
     * Removes a homework record from history.
     */
    @Delete
    suspend fun deleteHomework(homework: Homework)

    /**
     * Returns a reactive stream of all homework records for a specific student.
     */
    @Query("SELECT * FROM homework WHERE student_id = :studentId")
    fun getHomeworkForStudent(studentId: Long): Flow<List<Homework>>
}
