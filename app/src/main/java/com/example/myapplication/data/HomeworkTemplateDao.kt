package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for homework templates.
 *
 * This DAO provides the interface for managing reusable [HomeworkTemplate] definitions,
 * which serve as the blueprints for multi-step homework checking routines.
 */
@Dao
interface HomeworkTemplateDao {
    /**
     * Persists a new homework template.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(homeworkTemplate: HomeworkTemplate)

    /**
     * Bulk inserts or replaces multiple [HomeworkTemplate] records.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(homeworkTemplates: List<HomeworkTemplate>)

    /**
     * Updates an existing homework template's metadata or scoring structure.
     */
    @Update
    suspend fun update(homeworkTemplate: HomeworkTemplate)

    /**
     * Deletes a template definition.
     */
    @Delete
    suspend fun delete(homeworkTemplate: HomeworkTemplate)

    /**
     * Streams all available templates, sorted alphabetically by name.
     * @return A reactive [Flow] for UI observation.
     */
    @Query("SELECT * FROM homework_templates ORDER BY name ASC")
    fun getAllHomeworkTemplates(): Flow<List<HomeworkTemplate>>
}
