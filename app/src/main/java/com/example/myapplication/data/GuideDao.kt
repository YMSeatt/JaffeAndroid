package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GuideDao {
    @Query("SELECT * FROM guides")
    fun getAllGuides(): Flow<List<Guide>>

    @Insert
    suspend fun insert(guide: Guide)

    @Update
    suspend fun update(guide: Guide)

    @Delete
    suspend fun delete(guide: Guide)
}