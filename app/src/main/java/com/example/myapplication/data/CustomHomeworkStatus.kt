package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines a user-defined status label for homework completion.
 *
 * This entity allows teachers to create custom markers for the state of a homework
 * assignment (e.g., "Submitted Late", "Missing", "Redo Required"). These statuses
 * provide richer context than a simple binary "Complete/Incomplete" flag when
 * reviewing student progress.
 *
 * @property id Unique identifier for the homework status.
 * @property name The display name of the status label.
 */
@Entity(tableName = "custom_homework_statuses")
data class CustomHomeworkStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
