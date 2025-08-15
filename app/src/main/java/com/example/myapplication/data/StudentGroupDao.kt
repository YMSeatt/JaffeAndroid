package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudentGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: StudentGroup)

    @Update
    suspend fun update(group: StudentGroup)

    @Delete
    suspend fun delete(group: StudentGroup)

    @Query("SELECT * FROM student_groups ORDER BY name ASC")
    fun getAllGroups(): LiveData<List<StudentGroup>>

    @Query("SELECT * FROM student_groups WHERE id = :id")
    suspend fun getGroupById(id: Int): StudentGroup?
}
