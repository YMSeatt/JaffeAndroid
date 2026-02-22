package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Student table.
 *
 * This DAO provides the primary interface for managing student data, including
 * their basic info, seating chart positions, and complex UI styling overrides.
 */
@Dao
interface StudentDao {
    /**
     * Inserts or replaces a single [Student] record.
     * @return The row ID of the inserted record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    /**
     * Bulk inserts or replaces multiple [Student] records.
     * Used during initial data import or desktop synchronization.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<Student>): List<Long>

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Delete
    suspend fun deleteAll(students: List<Student>)

    /** Retrieves all students as a reactive [LiveData] stream. */
    @Query("SELECT * FROM students")
    fun getAllStudents(): LiveData<List<Student>>

    /** Non-reactive version of [getAllStudents] for background processing. */
    @Query("SELECT * FROM students")
    suspend fun getAllStudentsNonLiveData(): List<Student>

    /** Searches for students by first name, last name, or nickname. */
    @Query("SELECT * FROM students WHERE firstName LIKE :searchQuery OR lastName LIKE :searchQuery OR nickname LIKE :searchQuery")
    fun searchStudents(searchQuery: String): LiveData<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getStudentById(studentId: Long): LiveData<Student>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentByIdNonLiveData(studentId: Long): Student?

    @Query("SELECT * FROM students WHERE id IN (:studentIds)")
    suspend fun getStudentsByIdsList(studentIds: List<Long>): List<Student>

    @Deprecated("Use BehaviorEventDao.getRecentBehaviorEventsForStudent instead.", ReplaceWith("BehaviorEventDao.getRecentBehaviorEventsForStudent"))
    @Query("SELECT BE.* FROM behavior_events BE JOIN students S ON BE.studentId = S.id WHERE S.id = :studentId ORDER BY BE.timestamp DESC LIMIT :limit")
    fun getRecentBehaviorEventsForStudent(studentId: Long, limit: Int): LiveData<List<BehaviorEvent>>

    /**
     * Retrieves a student by their [Student.stringId].
     * String IDs are typically UUIDs imported from the Python desktop application.
     */
    @Query("SELECT * FROM students WHERE stringId = :stringId")
    suspend fun getStudentByStringId(stringId: String): Student?

    /**
     * Updates only the (X, Y) coordinates for a student.
     * Optimized for high-frequency drag-and-drop operations.
     */
    @Query("UPDATE students SET xPosition = :x, yPosition = :y WHERE id = :id")
    suspend fun updatePosition(id: Long, x: Float, y: Float)

    /** Bulk update for multiple student entities. */
    @Update
    suspend fun updateAll(students: List<Student>)

    /** Retrieves students belonging to any of the specified groups. */
    @Query("SELECT * FROM students WHERE groupId IN (:groupIds)")
    fun getStudentsByGroupIds(groupIds: List<Long>): Flow<List<Student>>

    /** Retrieves students who are not assigned to any group. */
    @Query("SELECT * FROM students WHERE groupId IS NULL")
    fun getUngroupedStudents(): Flow<List<Student>>

    /**
     * Checks if a student with the given first and last name already exists.
     * Case-insensitive. Used to prevent duplicates during manual addition or import.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE LOWER(firstName) = LOWER(:firstName) AND LOWER(lastName) = LOWER(:lastName) LIMIT 1)")
    suspend fun studentExists(firstName: String, lastName: String): Boolean
}
