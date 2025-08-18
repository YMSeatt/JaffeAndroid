package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentGroupDao {
    @Query("SELECT * FROM student_groups ORDER BY name ASC")
    fun getAllStudentGroups(): Flow<List<StudentGroup>>

    @Query("SELECT * FROM student_groups WHERE id = :groupId")
    suspend fun getStudentGroupById(groupId: Long): StudentGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentGroup: StudentGroup): Long

    @Update
    suspend fun update(studentGroup: StudentGroup)

    @Delete
    suspend fun delete(studentGroup: StudentGroup)

    @Query("DELETE FROM student_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)
}
