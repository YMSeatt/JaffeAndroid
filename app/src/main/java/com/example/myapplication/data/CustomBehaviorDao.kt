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
 * Data Access Object for [CustomBehavior].
 */
@Dao
interface CustomBehaviorDao {
    /**
     * Saves a new custom behavior or updates an existing one.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customBehavior: CustomBehavior)

    /**
     * Updates an existing custom behavior.
     */
    @Update
    suspend fun update(customBehavior: CustomBehavior)

    /**
     * Deletes a custom behavior from the database.
     */
    @Delete
    suspend fun delete(customBehavior: CustomBehavior)

    /**
     * Retrieves all custom behaviors, sorted alphabetically by name, as a reactive [LiveData] stream.
     */
    @Query("SELECT * FROM custom_behaviors ORDER BY name ASC")
    fun getAllCustomBehaviors(): LiveData<List<CustomBehavior>>

    /**
     * Deletes all custom behaviors from the table.
     */
    @Query("DELETE FROM custom_behaviors")
    suspend fun deleteAll()

    /**
     * Inserts a list of custom behaviors in a single operation.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(behaviors: List<CustomBehavior>)

    /**
     * Replaces all existing custom behaviors with a new list in an atomic transaction.
     * This is primarily used during data imports to ensure the custom behavior set is
     * fully refreshed without intermediate inconsistent states.
     *
     * @param behaviors The new list of behaviors to populate the table with.
     */
    @Transaction
    suspend fun replaceAll(behaviors: List<CustomBehavior>) {
        deleteAll()
        insertAll(behaviors)
    }
}
