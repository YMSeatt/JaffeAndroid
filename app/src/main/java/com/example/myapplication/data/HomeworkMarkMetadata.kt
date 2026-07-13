package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines a category of mark or status that can be applied to a homework assignment.
 *
 * This entity serves as the metadata definition for scoring homework logs. It allows
 * for mapping status labels (e.g., "Complete", "Signed") to numeric point values.
 *
 * Parity with Python's `homework_mark_types`.
 */
@Entity(tableName = "homework_mark_metadata")
data class HomeworkMarkMetadata(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** The human-readable label for the mark (e.g., "Complete", "Late"). */
    val name: String,
    /** The number of points awarded for this mark or status. */
    val defaultPoints: Double
)
