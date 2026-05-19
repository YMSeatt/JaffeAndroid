package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for visual alignment guides.
 */
@Dao
interface GuideDao {
    /**
     * Retrieves all guides as a reactive [Flow] stream.
     */
    @Query("SELECT * FROM guides")
    fun getAllGuides(): Flow<List<Guide>>

    /**
     * Inserts a new alignment guide.
     * @return The row ID of the inserted record.
     */
    @Insert
    suspend fun insert(guide: Guide): Long

    @Update
    suspend fun update(guide: Guide)

    @Delete
    suspend fun delete(guide: Guide)
}