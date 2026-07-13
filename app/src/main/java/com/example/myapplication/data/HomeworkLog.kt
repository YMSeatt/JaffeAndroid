package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a record of a student's homework completion status.
 *
 * This entity supports both simple status tracking (e.g., "Done", "Late") and complex
 * scoring via the [marksData] JSON field. This hybrid approach ensures that the database
 * remains flexible as classroom reporting requirements change.
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
    /**
     * A JSON string representing complex marks or effort scores.
     * This field is used to store dynamic scoring metrics that aren't captured by the
     * primary [status] field.
     */
    val marksData: String? = null,
    /** Whether the homework recording session is finished. */
    val isComplete: Boolean = false
)
