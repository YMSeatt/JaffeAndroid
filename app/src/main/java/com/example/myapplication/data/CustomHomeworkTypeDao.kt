package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

/**
 * Data Access Object for [CustomHomeworkType].
 */
@Dao
interface CustomHomeworkTypeDao {
    /**
     * Saves a new custom homework type or updates an existing one.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customHomeworkType: CustomHomeworkType)

    /**
     * Updates an existing custom homework type.
     */
    @Update
    suspend fun update(customHomeworkType: CustomHomeworkType)

    /**
     * Deletes a custom homework type from the database.
     */
    @Delete
    suspend fun delete(customHomeworkType: CustomHomeworkType)

    /**
     * Retrieves all custom homework types, sorted alphabetically by name, as a reactive [LiveData] stream.
     */
    @Query("SELECT * FROM custom_homework_types ORDER BY name ASC")
    fun getAllCustomHomeworkTypes(): LiveData<List<CustomHomeworkType>>

    /**
     * Fetches a static list of all custom homework types.
     */
    @Query("SELECT * FROM custom_homework_types ORDER BY name ASC")
    suspend fun getAllCustomHomeworkTypesList(): List<CustomHomeworkType>

    /**
     * Deletes all custom homework types from the table.
     */
    @Query("DELETE FROM custom_homework_types")
    suspend fun deleteAll()

    /**
     * Inserts a list of custom homework types in a single operation.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<CustomHomeworkType>)

    /**
     * Replaces all existing custom homework types with a new list in an atomic transaction.
     * This is primarily used during data imports to ensure the homework type set is
     * fully refreshed without intermediate inconsistent states.
     *
     * @param types The new list of types to populate the table with.
     */
    @Transaction
    suspend fun replaceAll(types: List<CustomHomeworkType>) {
        deleteAll()
        insertAll(types)
    }
}
