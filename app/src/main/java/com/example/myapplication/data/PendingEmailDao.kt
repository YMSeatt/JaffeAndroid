package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PendingEmailDao {
    @Insert
    suspend fun insert(pendingEmail: PendingEmail)

    @Query("SELECT * FROM pending_emails")
    suspend fun getAll(): List<PendingEmail>

    @Query("DELETE FROM pending_emails WHERE id = :id")
    suspend fun delete(id: Long)
}
