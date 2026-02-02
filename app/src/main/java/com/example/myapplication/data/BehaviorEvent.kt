package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a single behavior incident logged for a student.
 */
@Serializable
@Entity(
    tableName = "behavior_events",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class BehaviorEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** The ID of the student associated with this event. */
    val studentId: Long,
    /** The category of behavior (e.g., "Talking", "Great Participation"). */
    val type: String,
    /** The time the event occurred, in milliseconds since epoch. */
    val timestamp: Long,
    /** Optional notes about the incident. */
    val comment: String?,
    /** Duration in milliseconds that this event should be highlighted on the seating chart. */
    val timeout: Long = 0
)
