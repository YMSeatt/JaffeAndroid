package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for classroom furniture.
 */
@Dao
interface FurnitureDao {
    /**
     * Inserts or replaces a [Furniture] record.
     * @return The row ID of the inserted record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(furniture: Furniture): Long

    /**
     * Bulk inserts or replaces multiple [Furniture] records.
     * Used during initial data import or desktop synchronization.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(furniture: List<Furniture>): List<Long>

    @Update
    suspend fun update(furniture: Furniture)

    @Delete
    suspend fun delete(furniture: Furniture)

    /**
     * Deletes a furniture item by its ID.
     */
    @Query("DELETE FROM furniture WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Retrieves all furniture items as a reactive [Flow] stream.
     */
    @Query("SELECT * FROM furniture")
    fun getAllFurniture(): Flow<List<Furniture>>

    /**
     * Non-reactive version of [getAllFurniture] for background processing.
     */
    @Query("SELECT * FROM furniture")
    suspend fun getAllFurnitureList(): List<Furniture>

    /**
     * Fetches a single furniture item by its unique [Furniture.id].
     */
    @Query("SELECT * FROM furniture WHERE id = :id")
    suspend fun getFurnitureById(id: Long): Furniture?

    /**
     * Updates only the (X, Y) coordinates for a furniture item.
     * Optimized for high-frequency drag-and-drop operations on the canvas.
     */
    @Query("UPDATE furniture SET xPosition = :newX, yPosition = :newY WHERE id = :furnitureId")
    suspend fun updatePosition(furnitureId: Long, newX: Float, newY: Float)

    /**
     * Bulk updates multiple furniture entities.
     */
    @Update
    suspend fun updateAll(furniture: List<Furniture>)
}
