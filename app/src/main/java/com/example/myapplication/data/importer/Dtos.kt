package com.example.myapplication.data.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Main Data Transfer Object representing the root of the classroom data JSON
 * exported by the Python desktop application (version v10).
 *
 * @property students A map of student UUIDs to their detailed data.
 * @property furniture A map of furniture item UUIDs to their spatial and visual data.
 * @property behaviorLog A list of all behavior incidents and quiz sessions.
 * @property homeworkLog A list of all homework completion records.
 */
@Serializable
data class ClassroomDataDto(
    val students: Map<String, StudentDto>,
    val furniture: Map<String, FurnitureDto>,
    @SerialName("behavior_log")
    val behaviorLog: List<LogEntryDto>,
    @SerialName("homework_log")
    val homeworkLog: List<HomeworkLogEntryDto>
)

/**
 * DTO representing a student as defined in the Python desktop application.
 *
 * @property id The string-based UUID used by the Python application.
 * @property firstName The student's legal first name.
 * @property lastName The student's legal last name.
 * @property nickname An optional familiar name.
 * @property gender The student's gender (typically "Boy" or "Girl").
 * @property x The horizontal position on the 2000x1500 logical canvas.
 * @property y The vertical position on the 2000x1500 logical canvas.
 * @property width The custom width of the student's desk/box in pixels.
 * @property height The custom height of the student's desk/box in pixels.
 * @property groupId The string-based UUID of the group this student belongs to, if any.
 */
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

/**
 * DTO representing a piece of classroom furniture.
 *
 * @property id String-based UUID.
 * @property name Display name of the item (e.g., "Teacher Desk").
 * @property type The category of furniture.
 * @property x Horizontal canvas coordinate.
 * @property y Vertical canvas coordinate.
 * @property width Furniture width in logical pixels.
 * @property height Furniture height in logical pixels.
 * @property fillColor Hex string for the item's background color.
 * @property outlineColor Hex string for the item's border color.
 */
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

/**
 * DTO for a behavior log entry or a quiz session.
 *
 * @property timestamp ISO-8601 formatted date/time string.
 * @property studentId The UUID of the student associated with this event.
 * @property behavior The name of the behavior type or the quiz name.
 * @property comment User-entered notes about the incident.
 * @property type Discriminating field: "behavior" for standard incidents, "quiz" for academic checks.
 * @property scoreDetails Contains numeric results if [type] is "quiz".
 */
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

/**
 * Academic performance details for a quiz log entry.
 *
 * @property correct Number of questions answered correctly.
 * @property totalAsked Total number of questions presented in the session.
 */
@Serializable
data class ScoreDetailsDto(
    val correct: Int,
    @SerialName("total_asked")
    val totalAsked: Int
)

/**
 * DTO for a homework completion record.
 *
 * @property timestamp ISO-8601 formatted date/time string.
 * @property studentId The UUID of the student.
 * @property behavior Legacy field sometimes used for status; prefer [homeworkStatus].
 * @property homeworkType The name of the assignment or category.
 * @property homeworkStatus The specific completion state (e.g., "Done", "Late").
 * @property comment User notes.
 * @property type Discriminator for homework logs.
 */
@Serializable
data class HomeworkLogEntryDto(
    val timestamp: String,
    @SerialName("student_id")
    val studentId: String,
    val behavior: String,
    @SerialName("homework_type")
    val homeworkType: String? = null,
    @SerialName("homework_status")
    val homeworkStatus: String? = null,
    val comment: String? = null,
    val type: String
)

/**
 * DTO for student group definitions.
 * Maps to student_groups_v10.json.
 */
@Serializable
data class StudentGroupDto(
    val id: String,
    val name: String,
    val color: String
)

/**
 * DTO for user-defined behavior categories.
 * Maps to custom_behaviors_v10.json.
 */
@Serializable
data class CustomBehaviorDto(
    val name: String
)

/**
 * DTO for user-defined homework assignment types.
 * Maps to custom_homework_types_v10.json.
 */
@Serializable
data class CustomHomeworkTypeDto(
    val id: String,
    val name: String
)

/**
 * DTO for user-defined homework status labels.
 * Maps to custom_homework_statuses_v10.json.
 */
@Serializable
data class CustomHomeworkStatusDto(
    val name: String
)
