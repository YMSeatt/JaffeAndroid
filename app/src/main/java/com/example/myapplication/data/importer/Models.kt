package com.example.myapplication.data.importer

import kotlinx.serialization.Serializable

@Serializable
data class Classroom(
    val students: Map<String, Student>,
    val furniture: Map<String, Furniture>,
    val behavior_log: List<BehaviorLogEntry>,
    val homework_log: List<HomeworkLogEntry>,
    val settings: Settings,
    val last_excel_export_path: String?,
    val _per_student_last_cleared: Map<String, String>,
    val undo_stack: List<Map<String, @Serializable(with = AnySerializer::class) Any>>,
    val redo_stack: List<Map<String, @Serializable(with = AnySerializer::class) Any>>,
    val guides: Map<String, Guide>,
    val next_guide_id_num: Int
)

@Serializable
data class Student(
    val first_name: String,
    val last_name: String,
    val nickname: String,
    val full_name: String,
    val gender: String,
    val x: Double,
    val y: Double,
    val id: String,
    val width: Double,
    val height: Double,
    val original_next_id_num_after_add: Int,
    val group_id: String?,
    val style_overrides: StyleOverrides
)

@Serializable
data class StyleOverrides(
    val fill_color: String?,
    val outline_color: String?,
    val font_family: String?,
    val font_size: Int?,
    val font_color: String?,
    val width: Double?,
    val height: Double?
)

@Serializable
data class Furniture(
    val name: String,
    val type: String,
    val x: Double,
    val y: Double,
    val id: String,
    val width: Double,
    val height: Double,
    val fill_color: String,
    val outline_color: String,
    val original_next_id_num_after_add: Int
)

@Serializable
data class BehaviorLogEntry(
    val timestamp: String,
    val student_id: String,
    val student_first_name: String,
    val student_last_name: String,
    val behavior: String,
    val comment: String,
    val type: String,
    val day: String
)

@Serializable
data class HomeworkLogEntry(
    val timestamp: String,
    val student_id: String,
    val student_first_name: String,
    val student_last_name: String,
    val behavior: String,
    val homework_type: String,
    val homework_status: String,
    val comment: String,
    val type: String,
    val day: String,
    val marks_data: Map<String, Double>?,
    val num_items: Int?
)

@Serializable
data class Settings(
    val encrypt_data_files: Boolean,
    val app_password_hash: String?
)

@Serializable
data class Guide(
    val id: String,
    val type: String,
    val world_coord: Double
)
