package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_mark_types")
data class QuizMarkType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val defaultPoints: Double,
    val contributesToTotal: Boolean,
    val isExtraCredit: Boolean
)
