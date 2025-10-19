package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "email_schedules")
data class EmailSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Int, // Bitmask for days
    val recipientEmail: String,
    val subject: String,
    val body: String,
    val enabled: Boolean = true,
    val days: Set<String>
)
