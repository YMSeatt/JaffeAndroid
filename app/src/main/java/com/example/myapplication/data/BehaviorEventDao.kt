package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BehaviorEventDao {
    @Insert
    suspend fun insertBehaviorEvent(event: BehaviorEvent)

    @Delete
    suspend fun delete(event: BehaviorEvent)

    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getBehaviorEventsForStudent(studentId: Long): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM behavior_events ORDER BY timestamp DESC")
    fun getAllBehaviorEvents(): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM behavior_events ORDER BY timestamp DESC")
    suspend fun getAllBehaviorEventsList(): List<BehaviorEvent>

    @Query("SELECT * FROM behavior_events WHERE timestamp BETWEEN :startDate AND :endDate AND (:studentIds IS NULL OR studentId IN (:studentIds)) ORDER BY timestamp ASC")
    suspend fun getFilteredBehaviorEvents(startDate: Long, endDate: Long, studentIds: List<Long>?): List<BehaviorEvent>

    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentBehaviorForStudent(studentId: Long): BehaviorEvent?

    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBehaviorEventsForStudent(studentId: Long, limit: Int): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM behavior_events WHERE id = :id")
    suspend fun getBehaviorEventById(id: Long): BehaviorEvent?

    @Query("DELETE FROM behavior_events WHERE id = :id")
    suspend fun deleteBehaviorEventById(id: Long)
    
    @Update
    suspend fun updateBehaviorEvent(event: BehaviorEvent)
}
