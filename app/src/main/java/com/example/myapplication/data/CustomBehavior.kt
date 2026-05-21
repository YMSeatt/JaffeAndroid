package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user-defined category of student behavior.
 *
 * This entity allows teachers to customize the behavior logging system beyond the
 * standard [SystemBehavior] types. These custom categories (e.g., "Helping Others",
 * "Group Leadership") appear in the behavior entry UI, enabling more granular and
 * classroom-specific tracking.
 *
 * @property id Unique identifier for the custom behavior.
 * @property name The display name of the behavior category.
 */
@Entity(tableName = "custom_behaviors")
data class CustomBehavior(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
