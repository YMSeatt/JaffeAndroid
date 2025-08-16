package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val studentId: Long,
    val quizName: String,
    val markValue: Double?,
    val markType: String?,
    val maxMarkValue: Double?,
    val loggedAt: Long,
    val comment: String?,
    val marksData: String, // JSON string for marks data
    val numQuestions: Int
)
