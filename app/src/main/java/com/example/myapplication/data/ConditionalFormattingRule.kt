package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conditional_formatting_rules")
data class ConditionalFormattingRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val ruleType: String, // e.g., "behavior_count", "quiz_score_threshold", "group"
    val parameters: String, // JSON string of parameters, e.g., {"behavior_name": "Talking", "count_threshold": 3}
    val style: String, // JSON string of style to apply, e.g., {"backgroundColor": "#FFFF00", "outlineColor": "#FFA500"}
    val enabled: Boolean = true
)
