package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a reusable blueprint for a classroom quiz or assessment.
 *
 * ### Architectural Intent:
 * The [QuizTemplate] is the core of the "Normalized Assignment Model." It defines the
 * "shape" of an assessment once, which can then be reused to create individual [Quiz]
 * attempts for many students.
 *
 * ### Why use Templates?
 * 1. **Consistency**: Ensures that a "Midterm Quiz" follows the same scoring rules for
 *    everyone.
 * 2. **Efficiency**: Allows teachers to define question counts and default mark distributions
 *    (e.g., how many "Correct" marks are expected) once.
 * 3. **Rich Analytics**: Enables the system to compare student performance across specific
 *    standardized assessments.
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
