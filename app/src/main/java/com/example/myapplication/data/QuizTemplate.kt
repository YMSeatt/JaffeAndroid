package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_templates")
data class QuizTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val numQuestions: Int,
    val defaultMarks: Map<String, Int>
)
