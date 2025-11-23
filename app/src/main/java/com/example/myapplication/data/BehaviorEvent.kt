package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

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
    val studentId: Long,
    val type: String, // e.g., "Talking", "Out of Seat"
    val timestamp: Long,
    val comment: String?,
    val initials: String? = null,
    val timeout: Long = 0
)
