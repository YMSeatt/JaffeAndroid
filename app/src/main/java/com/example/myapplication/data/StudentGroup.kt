package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a logical grouping of students within a classroom.
 *
 * Student groups are used to organize the seating chart into collaborative clusters.
 * Each group has a unique name and an associated color, which is used to visually
 * differentiate students on the canvas.
 *
 * @property id Unique identifier for the group.
 * @property name The display name of the group (e.g., "Table 1", "Reading Group A").
 * @property color The visual identifier for the group, stored as a Hex color string.
 */
@Entity(tableName = "student_groups")
data class StudentGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String
)
