package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conditional_formatting_rules")
data class ConditionalFormattingRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val conditionJson: String,
    val formatJson: String,
    val targetType: String,
    val priority: Int
)