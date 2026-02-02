package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a record of a student's homework completion status.
 */
@Serializable
@Entity(
    tableName = "homework_logs",
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
data class HomeworkLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** The ID of the student associated with this log. */
    val studentId: Long,
    /** Name of the homework assignment or session. */
    val assignmentName: String,
    /** General status (e.g., "Done", "Not Done", "Signed"). */
    val status: String,
    val loggedAt: Long = System.currentTimeMillis(),
    val comment: String? = null,
    /** A JSON string representing complex marks or effort scores. */
    val marksData: String? = null,
    /** Whether the homework recording session is finished. */
    val isComplete: Boolean = false
)
