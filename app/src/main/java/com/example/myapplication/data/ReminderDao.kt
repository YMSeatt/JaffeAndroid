package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [Reminder] entity.
 * Provides methods for querying, inserting, updating, and deleting reminders.
 */
@Dao
interface ReminderDao {
    /**
     * Retrieves all reminders from the database as a reactive stream.
     */
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<Reminder>>

    /**
     * Inserts a new reminder into the database.
     * @return The row ID of the newly inserted reminder.
     */
    @Insert
    suspend fun insert(reminder: Reminder): Long

    /**
     * Updates an existing reminder in the database.
     */
    @Update
    suspend fun update(reminder: Reminder)

    /**
     * Deletes a reminder by its unique identifier.
     * @param id The ID of the reminder to delete.
     */
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)
}