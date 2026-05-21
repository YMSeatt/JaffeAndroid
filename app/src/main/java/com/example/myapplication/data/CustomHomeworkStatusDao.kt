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
 * Data Access Object for [CustomHomeworkStatus].
 */
@Dao
interface CustomHomeworkStatusDao {
    /**
     * Saves a new custom homework status or updates an existing one.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customHomeworkStatus: CustomHomeworkStatus)

    /**
     * Updates an existing custom homework status.
     */
    @Update
    suspend fun update(customHomeworkStatus: CustomHomeworkStatus)

    /**
     * Deletes a custom homework status from the database.
     */
    @Delete
    suspend fun delete(customHomeworkStatus: CustomHomeworkStatus)

    /**
     * Retrieves all custom homework statuses, sorted alphabetically by name, as a reactive [LiveData] stream.
     */
    @Query("SELECT * FROM custom_homework_statuses ORDER BY name ASC")
    fun getAllCustomHomeworkStatuses(): LiveData<List<CustomHomeworkStatus>>

    /**
     * Fetches a static list of all custom homework statuses.
     */
    @Query("SELECT * FROM custom_homework_statuses ORDER BY name ASC")
    suspend fun getAllCustomHomeworkStatusesList(): List<CustomHomeworkStatus>

    /**
     * Deletes all custom homework statuses from the table.
     */
    @Query("DELETE FROM custom_homework_statuses")
    suspend fun deleteAll()

    /**
     * Inserts a list of custom homework statuses in a single operation.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statuses: List<CustomHomeworkStatus>)

    /**
     * Replaces all existing custom homework statuses with a new list in an atomic transaction.
     * This is primarily used during data imports to ensure the homework status set is
     * fully refreshed without intermediate inconsistent states.
     *
     * @param statuses The new list of statuses to populate the table with.
     */
    @Transaction
    suspend fun replaceAll(statuses: List<CustomHomeworkStatus>) {
        deleteAll()
        insertAll(statuses)
    }
}
