package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a persistent snapshot of a classroom's physical and student layout.
 *
 * Layout templates allow teachers to save and quickly restore specific seating
 * arrangements (e.g., "Exam Mode", "Group Work").
 *
 * @property id Unique identifier for the template.
 * @property name A user-defined name for the template.
 * @property layoutDataJson A JSON string containing the serialized state of all students
 *           and furniture on the canvas at the time the template was saved.
 */
@Serializable
@Entity(tableName = "layout_templates")
data class LayoutTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val layoutDataJson: String
)
