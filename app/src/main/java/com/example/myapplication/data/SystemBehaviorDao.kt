package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for standardized [SystemBehavior] options.
 *
 * This DAO manages the baseline feedback categories used in the behavior entry UI.
 * System behaviors are typically read-only from the user's perspective, providing
 * a fixed set of options for consistent reporting.
 */
@Dao
interface SystemBehaviorDao {
    /**
     * Retrieves all available system-level behavior options as a reactive [Flow].
     *
     * This stream is observed by the UI to populate the initial feedback options
     * in the behavior logging menus.
     */
    @Query("SELECT * FROM system_behaviors")
    fun getAllSystemBehaviors(): Flow<List<SystemBehavior>>

    /**
     * Inserts a new system behavior option into the database.
     *
     * This method is primarily used during database bootstrapping, factory resets,
     * or room migrations (e.g., version 22) to establish the standardized set
     * of feedback categories.
     */
    @Insert
    suspend fun insert(systemBehavior: SystemBehavior)
}