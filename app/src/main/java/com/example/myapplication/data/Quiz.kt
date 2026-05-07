package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents an individual student's performance on a specific [QuizTemplate].
 *
 * ### Normalized Assignment Model
 * This entity is part of the "Normalized Assignment Model," which provides a structured,
 * relational alternative to the legacy [QuizLog]. While [QuizLog] uses a flexible JSON-backed
 * approach for rapid, unstructured data entry, the [Quiz] entity links directly to a
 * [QuizTemplate] blueprint.
 *
 * ### Key Benefits:
 * 1. **Relational Integrity**: Maintains strict foreign key links to students and templates.
 * 2. **Standardized Analysis**: Facilitates longitudinal analysis of specific assessments
 *    (e.g., comparing "Unit 1 Quiz" results across multiple classes) because they share a
 *    common template ID.
 * 3. **Schema Stability**: Separates the assessment definition (Template) from the student's
 *    performance (Quiz attempt).
 *
 * @property id Unique identifier for this quiz attempt.
 * @property studentId Foreign key referencing the [Student] who took the quiz.
 * @property templateId Foreign key referencing the [QuizTemplate] blueprint used.
 * @property score The final calculated score for the attempt.
 * @property timestamp The time the assessment was completed.
 */
@Entity(
    tableName = "quizzes",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuizTemplate::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Quiz(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "student_id", index = true) val studentId: Long,
    @ColumnInfo(name = "template_id", index = true) val templateId: Long?,
    val score: Double,
    val timestamp: Long = System.currentTimeMillis()
)
