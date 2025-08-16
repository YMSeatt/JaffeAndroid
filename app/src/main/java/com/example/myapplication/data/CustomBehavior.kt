package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_behaviors")
data class CustomBehavior(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
