package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a standardized, system-level feedback option for student behavior.
 *
 * Unlike [CustomBehavior], which are defined by individual teachers, System Behaviors
 * provide a consistent set of "hardcoded" feedback categories that can be used across
 * different classrooms for global reporting and analytics.
 *
 * These options are typically populated during database initialization or migrations
 * (e.g., Migration v22) and serve as the baseline for the behavior logging UI.
 *
 * @property id Unique identifier for the system behavior.
 * @property name The short label for the behavior (e.g., "Participating", "Disruptive").
 * @property description A more detailed explanation of what the behavior entails.
 */
@Serializable
@Entity(tableName = "system_behaviors")
data class SystemBehavior(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String
)