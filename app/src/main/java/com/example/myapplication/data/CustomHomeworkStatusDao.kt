package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CustomHomeworkStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customHomeworkStatus: CustomHomeworkStatus)

    @Update
    suspend fun update(customHomeworkStatus: CustomHomeworkStatus)

    @Delete
    suspend fun delete(customHomeworkStatus: CustomHomeworkStatus)

    @Query("SELECT * FROM custom_homework_statuses ORDER BY name ASC")
    fun getAllCustomHomeworkStatuses(): LiveData<List<CustomHomeworkStatus>>
}
