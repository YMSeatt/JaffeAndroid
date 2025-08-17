package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_groups")
data class StudentGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String // Hex color string
)
