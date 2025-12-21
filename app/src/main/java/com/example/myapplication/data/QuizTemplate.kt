package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(tableName = "quiz_templates")
data class QuizTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val numQuestions: Int,
    val defaultMarks: Map<String, Int>
)

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
