package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_mark_types",
    indices = [Index(value = ["python_id"], unique = true)]
)
data class QuizMarkType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "python_id")
    val pythonId: String,
    val name: String,
    val defaultPoints: Double,
    val contributesToTotal: Boolean,
    val isExtraCredit: Boolean
)
