package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [QuizMarkType] entities.
 *
 * This DAO provides the interface for persisting and querying the metadata that defines
 * how granular quiz marks (e.g., "Correct", "Half Credit") are translated into numeric
 * point values.
 */
@Dao
interface QuizMarkTypeDao {
    /**
     * Inserts a single mark type. If a mark type with the same ID already exists,
     * it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quizMarkType: QuizMarkType)

    /**
     * Updates an existing mark type's configuration (e.g., changing its default points).
     */
    @Update
    suspend fun update(quizMarkType: QuizMarkType)

    /**
     * Removes a mark type from the database.
     */
    @Delete
    suspend fun delete(quizMarkType: QuizMarkType)

    /**
     * Observes all available mark types, sorted alphabetically by name.
     */
    @Query("SELECT * FROM quiz_mark_types ORDER BY name ASC")
    fun getAllQuizMarkTypes(): Flow<List<QuizMarkType>>

    /**
     * Retrieves a one-shot list of all mark types.
     */
    @Query("SELECT * FROM quiz_mark_types ORDER BY name ASC")
    suspend fun getAllQuizMarkTypesList(): List<QuizMarkType>

    /**
     * Retrieves a specific mark type by its unique database ID.
     */
    @Query("SELECT * FROM quiz_mark_types WHERE id = :id")
    suspend fun getQuizMarkTypeById(id: Long): QuizMarkType?

    /**
     * Clears all mark type metadata from the database.
     */
    @Query("DELETE FROM quiz_mark_types")
    suspend fun deleteAll()

    /**
     * Inserts multiple mark types in a single operation.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(markTypes: List<QuizMarkType>)

    /**
     * Atomically replaces the entire mark type library.
     * This is commonly used during data synchronization or when resetting to default values.
     */
    @Transaction
    suspend fun replaceAll(markTypes: List<QuizMarkType>) {
        deleteAll()
        insertAll(markTypes)
    }
}
