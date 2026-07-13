package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a record of a student's performance on a quiz.
 *
 * This entity utilizes the "JSON-Backed Flexibility" strategy. To avoid frequent schema
 * migrations when adding new scoring types (e.g., "Extra Credit", "Participation"),
 * granular marks are stored in the [marksData] JSON field.
 *
 * ### Lifecycle:
 * - **In-Progress**: During a live session, [isComplete] is `false`. Logs are held in the
 *   ViewModel's session buffer.
 * - **Finalized**: Once the session ends, logs are persisted to the database with [isComplete] set to `true`.
 */
@Serializable
@Entity(
    tableName = "quiz_logs",
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
data class QuizLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** The ID of the student who took the quiz. */
    val studentId: Long,
    /** Name of the quiz (e.g., "Midterm", "Pop Quiz #1"). */
    val quizName: String,
    /** @deprecated Legacy single mark value. Use [marksData] instead. */
    val markValue: Double?,
    /** @deprecated Legacy single mark type label. */
    val markType: String?,
    /** @deprecated Legacy max points value. */
    val maxMarkValue: Double?,
    /** The time the quiz was logged. */
    val loggedAt: Long,
    val comment: String?,
    /**
     * A JSON string representing a map of mark type names to their counts.
     * Example: `{"Correct": 8, "Incorrect": 2, "Partially Correct": 1}`.
     * This allows for dynamic scoring metrics without modifying the SQLite schema.
     */
    val marksData: String,
    /** Total number of questions in the quiz. */
    val numQuestions: Int,
    /** Whether the quiz recording session is finished. */
    val isComplete: Boolean = false
)
