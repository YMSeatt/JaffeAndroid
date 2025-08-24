package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students")
    fun getAllStudents(): LiveData<List<Student>>

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsNonLiveData(): List<Student>

    @Query("SELECT * FROM students WHERE firstName LIKE :searchQuery OR lastName LIKE :searchQuery OR nickname LIKE :searchQuery")
    fun searchStudents(searchQuery: String): LiveData<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getStudentById(studentId: Long): LiveData<Student>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentByIdNonLiveData(studentId: Long): Student?

    @Query("SELECT s.id, s.firstName, s.lastName, s.initials, s.xPosition, s.yPosition, s.customWidth, s.customHeight, s.customBackgroundColor, s.customOutlineColor, s.customTextColor FROM students s")
    fun getStudentsForDisplay(): LiveData<List<StudentDetailsForDisplay>>

    @Query("SELECT BE.* FROM behavior_events BE JOIN students S ON BE.studentId = S.id WHERE S.id = :studentId ORDER BY BE.timestamp DESC LIMIT :limit")
    fun getRecentBehaviorEventsForStudent(studentId: Long, limit: Int): LiveData<List<BehaviorEvent>>

    @Query("SELECT * FROM students WHERE stringId = :stringId")
    suspend fun getStudentByStringId(stringId: String): Student?

    @Query("UPDATE students SET xPosition = :x, yPosition = :y WHERE id = :id")
    suspend fun updatePosition(id: Long, x: Float, y: Float)

    @Update
    suspend fun updateAll(students: List<Student>)

    @Query("SELECT * FROM students WHERE groupId IN (:groupIds)")
    fun getStudentsByGroupIds(groupIds: List<Long>): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE groupId IS NULL")
    fun getUngroupedStudents(): Flow<List<Student>>

    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE firstName = :firstName AND lastName = :lastName LIMIT 1)")
    suspend fun studentExists(firstName: String, lastName: String): Boolean
}
