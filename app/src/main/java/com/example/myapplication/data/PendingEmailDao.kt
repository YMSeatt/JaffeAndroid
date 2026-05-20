package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data Access Object for the pending email reliability queue.
 *
 * This DAO manages the lifecycle of [PendingEmail] entities, which represent
 * messages that were not successfully sent during their initial attempt.
 */
@Dao
interface PendingEmailDao {
    /**
     * Enqueues a failed or deferred email for future retry.
     */
    @Insert
    suspend fun insert(pendingEmail: PendingEmail)

    /**
     * Retrieves all messages currently in the reliability queue.
     */
    @Query("SELECT * FROM pending_emails")
    suspend fun getAll(): List<PendingEmail>

    /**
     * Removes a message from the queue by its unique ID.
     */
    @Query("DELETE FROM pending_emails WHERE id = :id")
    suspend fun delete(id: Long)
}
