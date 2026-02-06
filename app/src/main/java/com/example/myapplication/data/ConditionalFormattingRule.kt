package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A Room database entity representing a conditional formatting rule.
 * These rules are used to dynamically change the visual appearance of students or furniture
 * in the seating chart based on behavioral data, quiz scores, or current session status.
 *
 * Evaluation is handled by the `ConditionalFormattingEngine`.
 *
 * @property id Unique primary key for the rule.
 * @property name A human-readable name for the rule (e.g., "Failing Students").
 * @property type The broad category of the rule (e.g., "group", "behavior").
 * @property conditionJson A JSON string representing the [Condition] data class,
 *                         defining the criteria for this rule.
 * @property formatJson A JSON string representing the [Format] data class,
 *                       defining the visual styles (color, outline) to apply.
 * @property targetType What the rule applies to (currently defaults to "student").
 * @property priority The order in which rules are applied. Lower values have higher priority.
 */
@Entity(tableName = "conditional_formatting_rules")
data class ConditionalFormattingRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val type: String = "group",
    val conditionJson: String = "{}",
    val formatJson: String = "{}",
    val targetType: String = "student",
    val priority: Int = 0
)