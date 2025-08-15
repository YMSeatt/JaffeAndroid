package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students ORDER BY lastName ASC")
    fun getAllStudents(): LiveData<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getStudentById(studentId: Int): LiveData<Student>

    @Query("SELECT * FROM students WHERE id = :studentId") // Non-LiveData version
    suspend fun getStudentByIdNonLiveData(studentId: Int): Student? // Suspend function for use in coroutines

    @Query("""
        SELECT s.id, s.firstName, s.lastName, s.initials, s.xPosition, s.yPosition,
               s.customWidth, s.customHeight, s.customBackgroundColor, s.customOutlineColor, s.customTextColor,
               (SELECT be.type || CASE WHEN be.comment IS NOT NULL AND be.comment != '' THEN ': ' || be.comment ELSE '' END 
                FROM behavior_events be 
                WHERE be.studentId = s.id 
                ORDER BY be.timestamp DESC 
                LIMIT 1) as recentBehaviorDescription
        FROM students s
    """)
    fun getStudentsForDisplay(): LiveData<List<StudentDetailsForDisplay>>

    @Query("SELECT * FROM students WHERE groupId = :groupId ORDER BY lastName ASC")
    fun getStudentsByGroupId(groupId: Int): LiveData<List<Student>>
}
