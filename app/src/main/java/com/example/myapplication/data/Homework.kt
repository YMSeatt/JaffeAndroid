package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents an individual student's completion record for a [HomeworkTemplate].
 *
 * ### Normalized Assignment Model
 * This entity is part of the structured "Normalized Assignment Model." It provides a relational
 * link between a student and a standardized [HomeworkTemplate].
 *
 * ### Architectural Contrast:
 * - **Legacy Logs ([HomeworkLog])**: Best for rapid, unstructured, one-off notes or check-ins
 *   where the criteria might vary significantly between students.
 * - **Normalized Assignments ([Homework])**: Preferred for standardized classroom assessments
 *   and structured routines. They ensure that all students are checked against the same
 *   multi-step criteria defined in a [HomeworkTemplate].
 *
 * ### Usage:
 * Once a [HomeworkTemplate] is created, a [Homework] record is generated for each student
 * to track their progress and status relative to that specific blueprint.
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
