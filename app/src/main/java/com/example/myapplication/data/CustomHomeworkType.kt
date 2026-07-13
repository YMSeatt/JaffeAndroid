package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines a user-defined category for homework assignments.
 *
 * This entity allows teachers to group assignments into meaningful categories (e.g.,
 * "Reading", "Project", "Worksheet"). These types are used to organize the homework
 * list and can be used for filtering in reports and analytics.
 *
 * @property id Unique identifier for the homework type.
 * @property name The display name of the homework category.
 */
@Entity(tableName = "custom_homework_types")
data class CustomHomeworkType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
