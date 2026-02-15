package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT * FROM custom_homework_statuses ORDER BY name ASC")
    suspend fun getAllCustomHomeworkStatusesList(): List<CustomHomeworkStatus>

    @Query("DELETE FROM custom_homework_statuses")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statuses: List<CustomHomeworkStatus>)

    @Transaction
    suspend fun replaceAll(statuses: List<CustomHomeworkStatus>) {
        deleteAll()
        insertAll(statuses)
    }
}
