package com.example.myapplication.data.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DTO for classroom_data_v10.json
@Serializable
data class ClassroomDataDto(
    val students: Map<String, StudentDto>,
    val furniture: Map<String, FurnitureDto>,
    @SerialName("behavior_log")
    val behaviorLog: List<LogEntryDto>,
    @SerialName("homework_log")
    val homeworkLog: List<HomeworkLogEntryDto>
    // Ignoring settings and other fields for now
)

@Serializable
data class StudentDto(
    val id: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val nickname: String? = null,
    val gender: String,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    @SerialName("group_id")
    val groupId: String? = null
)

@Serializable
data class FurnitureDto(
    val id: String,
    val name: String,
    val type: String,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    @SerialName("fill_color")
    val fillColor: String? = null,
    @SerialName("outline_color")
    val outlineColor: String? = null
)

@Serializable
data class LogEntryDto(
    val timestamp: String,
    @SerialName("student_id")
    val studentId: String,
    val behavior: String,
    val comment: String? = null,
    val type: String, // "behavior", "quiz"
    @SerialName("score_details")
    val scoreDetails: ScoreDetailsDto? = null
)

@Serializable
data class ScoreDetailsDto(
    val correct: Int,
    @SerialName("total_asked")
    val totalAsked: Int
)

@Serializable
data class HomeworkLogEntryDto(
    val timestamp: String,
    @SerialName("student_id")
    val studentId: String,
    val behavior: String, // This seems to be used for status sometimes
    @SerialName("homework_type")
    val homeworkType: String? = null,
    @SerialName("homework_status")
    val homeworkStatus: String? = null,
    val comment: String? = null,
    val type: String
)

// DTO for student_groups_v10.json
@Serializable
data class StudentGroupDto(
    val id: String,
    val name: String,
    val color: String
)

// DTO for custom_behaviors_v10.json
@Serializable
data class CustomBehaviorDto(
    val name: String
)

// DTO for custom_homework_types_v10.json
@Serializable
data class CustomHomeworkTypeDto(
    val id: String,
    val name: String
)

// DTO for custom_homework_statuses_v10.json
@Serializable
data class CustomHomeworkStatusDto(
    val name: String
)
