package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * Defines the supported input types for a homework assignment step.
 */
enum class HomeworkMarkType {
    /** A simple binary toggle (e.g., "Signed by Parent"). */
    CHECKBOX,
    /** A numeric score or rating (e.g., "Effort: 4/5"). */
    SCORE,
    /** A free-text observation or note. */
    COMMENT
}

/**
 * Represents a single component or "step" of a homework assignment.
 *
 * A homework assignment can consist of multiple parts (e.g., "Math Exercises", "Parent Signature").
 * Each step has a specific input type and optional maximum value.
 *
 * @property id Unique identifier for the step (UUID generated at creation).
 * @property label The display name of the step (e.g., "Reading Log").
 * @property type The input interaction style (Checkbox, Score, or Comment).
 * @property maxValue The maximum allowed value for [HomeworkMarkType.SCORE] types.
 */
data class HomeworkMarkStep(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val type: HomeworkMarkType,
    val maxValue: Int = 1
)

/**
 * Represents a reusable blueprint for a complex homework assignment or check-in.
 *
 * A template defines a sequence of [HomeworkMarkStep]s. This allows teachers to
 * create standardized "Checking" routines that are reused across different sessions.
 *
 * @property id Unique identifier for the template.
 * @property name The display name of the assignment template (e.g., "Weekly Math Pack").
 * @property marksData A JSON-serialized list of [HomeworkMarkStep] objects. This structure
 *           provides flexibility for ad-hoc assignment changes without schema migrations.
 */
@Entity(tableName = "homework_templates")
data class HomeworkTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val marksData: String // JSON string for List<HomeworkMarkStep>
) {
    /**
     * Deserializes the [marksData] into a list of [HomeworkMarkStep] objects.
     */
    fun getSteps(): List<HomeworkMarkStep> {
        return try {
            val type = object : TypeToken<List<HomeworkMarkStep>>() {}.type
            Gson().fromJson(marksData, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        /**
         * Helper to create a [HomeworkTemplate] from a list of steps.
         */
        fun fromSteps(name: String, steps: List<HomeworkMarkStep>, id: Long = 0): HomeworkTemplate {
            return HomeworkTemplate(
                id = id,
                name = name,
                marksData = Gson().toJson(steps)
            )
        }
    }
}

/**
 * Represents an individual student's completion record for a [HomeworkTemplate].
 *
 * While legacy [HomeworkLog] entities are used for quick, unstructured notes,
 * the [Homework] entity provides a relational link to standardized templates.
 *
 * @property id Unique identifier for this homework record.
 * @property studentId Foreign key referencing the [Student].
 * @property templateId Foreign key referencing the [HomeworkTemplate] blueprint.
 * @property status A summary status of the assignment (e.g., "Incomplete", "Satisfactory").
 * @property timestamp The time the homework status was recorded.
 */
@Entity(
    tableName = "homework",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HomeworkTemplate::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "student_id", index = true) val studentId: Long,
    @ColumnInfo(name = "template_id", index = true) val templateId: Long?,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)
