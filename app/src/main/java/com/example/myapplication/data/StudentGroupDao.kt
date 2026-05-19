package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for student groups.
 *
 * Provides methods for managing student groups, including retrieval by ID or name,
 * and standard CRUD operations.
 */
@Dao
interface StudentGroupDao {
    /**
     * Retrieves all student groups as a reactive [Flow] stream, sorted by name.
     */
    @Query("SELECT * FROM student_groups ORDER BY name ASC")
    fun getAllStudentGroups(): Flow<List<StudentGroup>>

    /**
     * Non-reactive version of [getAllStudentGroups] for background processing.
     */
    @Query("SELECT * FROM student_groups ORDER BY name ASC")
    suspend fun getAllStudentGroupsList(): List<StudentGroup>

    /**
     * Fetches a group by its unique [StudentGroup.id].
     */
    @Query("SELECT * FROM student_groups WHERE id = :groupId")
    suspend fun getStudentGroupById(groupId: Long): StudentGroup?

    /**
     * Retrieves a group by its [StudentGroup.name].
     * Used during data ingestion (e.g., [com.example.myapplication.util.ExcelImportUtil])
     * to resolve group relationships from strings.
     */
    @Query("SELECT * FROM student_groups WHERE name = :name")
    suspend fun getGroupByName(name: String): StudentGroup?

    /**
     * Inserts or replaces a [StudentGroup].
     * @return The row ID of the inserted record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentGroup: StudentGroup): Long

    @Update
    suspend fun update(studentGroup: StudentGroup)

    @Delete
    suspend fun delete(studentGroup: StudentGroup)

    /**
     * Deletes a group by its ID.
     */
    @Query("DELETE FROM student_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)
}
