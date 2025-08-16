package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_homework_statuses")
data class CustomHomeworkStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
