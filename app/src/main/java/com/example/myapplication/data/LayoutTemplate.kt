package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "layout_templates")
data class LayoutTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val layoutData: String // JSON string containing lists of students and furniture with their positions
)
