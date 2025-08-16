package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BehaviorEventDao {
    @Insert
    suspend fun insertBehaviorEvent(event: BehaviorEvent)

    @Delete
    suspend fun delete(event: BehaviorEvent)

    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getBehaviorEventsForStudent(studentId: Int): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM behavior_events ORDER BY timestamp DESC")
    fun getAllBehaviorEvents(): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBehaviorEventsForStudent(studentId: Int, limit: Int): LiveData<List<BehaviorEvent>>
}
