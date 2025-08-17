package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentGroupDao {
    @Query("SELECT * FROM student_groups ORDER BY name ASC")
    fun getAllStudentGroups(): LiveData<List<StudentGroup>>

    @Query("SELECT * FROM student_groups WHERE id = :groupId")
    suspend fun getStudentGroupById(groupId: Long): StudentGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentGroup(studentGroup: StudentGroup): Long

    @Update
    suspend fun updateStudentGroup(studentGroup: StudentGroup)

    @Delete
    suspend fun deleteStudentGroup(studentGroup: StudentGroup)

    @Query("DELETE FROM student_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)
}
