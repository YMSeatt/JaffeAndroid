package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemBehaviorDao {
    @Query("SELECT * FROM system_behaviors")
    fun getAllSystemBehaviors(): Flow<List<SystemBehavior>>

    @Insert
    suspend fun insert(systemBehavior: SystemBehavior)
}