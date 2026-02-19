package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for student behavior events.
 *
 * This DAO manages the persistence and retrieval of both positive and negative
 * behavioral logs, supporting filtering for temporal UI display and reporting.
 */
@Dao
interface BehaviorEventDao {
    @Insert
    suspend fun insert(event: BehaviorEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<BehaviorEvent>): List<Long>

    @Delete
    suspend fun delete(event: BehaviorEvent)

    /** Retrieves all behavior logs for a specific student, most recent first. */
    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getBehaviorEventsForStudent(studentId: Long): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM behavior_events ORDER BY timestamp DESC")
    fun getAllBehaviorEvents(): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM behavior_events ORDER BY timestamp DESC")
    suspend fun getAllBehaviorEventsList(): List<BehaviorEvent>

    /** Retrieves behavior logs within a specific date range, used for Excel exports. */
    @Query("SELECT * FROM behavior_events WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    suspend fun getFilteredBehaviorEvents(startDate: Long, endDate: Long): List<BehaviorEvent>

    @Query("SELECT * FROM behavior_events WHERE timestamp BETWEEN :startDate AND :endDate AND studentId IN (:studentIds) ORDER BY timestamp ASC")
    suspend fun getFilteredBehaviorEventsWithStudents(startDate: Long, endDate: Long, studentIds: List<Long>): List<BehaviorEvent>

    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentBehaviorForStudent(studentId: Long): BehaviorEvent?

    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBehaviorEventsForStudent(studentId: Long, limit: Int): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM behavior_events WHERE studentId = :studentId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentBehaviorEventsForStudentList(studentId: Long, limit: Int): List<BehaviorEvent>

    /**
     * Retrieves recent behavior events for UI display on student icons, applying
     * multiple visibility filters:
     *
     * 1. **User Clearance**: Excludes logs that occurred before the student's [lastCleared] timestamp.
     * 2. **Temporal Decay**: Excludes logs older than [behaviorDisplayTimeout] hours,
     *    as configured in application preferences.
     * 3. **Concurrency**: Limited to the most recent [limit] events to keep the UI clean.
     */
    @Query("""
        SELECT * FROM behavior_events 
        WHERE studentId = :studentId 
        AND timestamp > :lastCleared 
        AND (:behaviorDisplayTimeout = 0 OR :currentTime < timestamp + (:behaviorDisplayTimeout * 3600000)) 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getRecentBehaviorEventsForStudentListFiltered(
        studentId: Long,
        limit: Int,
        lastCleared: Long,
        behaviorDisplayTimeout: Int,
        currentTime: Long
    ): List<BehaviorEvent>

    @Query("SELECT * FROM behavior_events WHERE id = :id")
    suspend fun getBehaviorEventById(id: Long): BehaviorEvent?

    @Query("DELETE FROM behavior_events WHERE id = :id")
    suspend fun deleteBehaviorEventById(id: Long)
    
    @Update
    suspend fun updateBehaviorEvent(event: BehaviorEvent)

    @Query("DELETE FROM behavior_events WHERE studentId = :studentId")
    suspend fun deleteByStudentId(studentId: Long)
}
