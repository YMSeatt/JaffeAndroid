package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guides")
data class Guide(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: GuideType,
    var position: Float
)

enum class GuideType {
    VERTICAL,
    HORIZONTAL
}