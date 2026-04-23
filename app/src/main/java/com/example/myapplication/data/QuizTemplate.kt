package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.ColumnInfo
import androidx.room.ForeignKey

/**
 * Represents a reusable blueprint for a classroom quiz or assessment.
 *
 * Unlike legacy [QuizLog] entities which use a hybrid JSON approach for all data,
 * the [QuizTemplate] and [Quiz] model follows a normalized strategy. A template
 * defines the "shape" of an assessment (name, question count, and default scoring),
 * which can then be instantiated multiple times for different students.
 *
 * @property id Unique identifier for the template.
 * @property name The display name of the quiz (e.g., "Unit 1 Vocabulary").
 * @property numQuestions The total number of questions or items in this quiz.
 * @property defaultMarks A mapping of mark type names (e.g., "Correct", "Half Credit")
 *           to their default occurrences or weights. This allows for rapid session setup.
 */
@Entity(tableName = "quiz_templates")
data class QuizTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val numQuestions: Int,
    val defaultMarks: Map<String, Int>
)

/**
 * Represents an individual student's performance on a specific [QuizTemplate].
 *
 * This entity links a student's final score and attempt timestamp to a reusable template.
 * It provides a more structured relational alternative to the legacy [QuizLog] for
 * high-level academic tracking.
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
