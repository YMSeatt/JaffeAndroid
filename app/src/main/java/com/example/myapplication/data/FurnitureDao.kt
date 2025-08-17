package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FurnitureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(furniture: Furniture)

    @Update
    suspend fun update(furniture: Furniture)

    @Delete
    suspend fun delete(furniture: Furniture)

    @Query("DELETE FROM furniture WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM furniture")
    fun getAllFurniture(): Flow<List<Furniture>>

    @Query("SELECT * FROM furniture WHERE id = :id")
    suspend fun getFurnitureById(id: Long): Furniture?

    @Query("UPDATE furniture SET xPosition = :newX, yPosition = :newY WHERE id = :furnitureId")
    suspend fun updatePosition(furnitureId: Long, newX: Float, newY: Float)

    @Update
    suspend fun updateAll(furniture: List<Furniture>)
}
