package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "furniture")
data class Furniture(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stringId: String? = null, // For JSON sync, e.g., "furniture_1"
    val name: String,
    val type: String, // e.g., "desk", "bookshelf"
    var xPosition: Float = 0f,
    var yPosition: Float = 0f,
    var width: Int,
    var height: Int,
    var fillColor: String? = null, // Hex string
    var outlineColor: String? = null // Hex string
)
