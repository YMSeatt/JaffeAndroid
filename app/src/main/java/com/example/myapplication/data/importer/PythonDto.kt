package com.example.myapplication.data.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PythonClassroomData(
    val students: Map<String, PythonStudent>,
    val furniture: Map<String, PythonFurniture>,
    @SerialName("behavior_log")
    val behaviorLog: List<PythonBehaviorLog>,
    @SerialName("homework_log")
    val homeworkLog: List<PythonHomeworkLog>
)

@Serializable
data class PythonStudent(
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val nickname: String,
    val gender: String,
    val x: Double,
    val y: Double,
    @SerialName("style_overrides")
    val styleOverrides: PythonStyleOverrides,
    @SerialName("group_id")
    val groupId: String
)

@Serializable
data class PythonStyleOverrides(
    val width: Double? = null,
    val height: Double? = null,
    @SerialName("fill_color")
    val fillColor: String? = null,
    @SerialName("outline_color")
    val outlineColor: String? = null,
    @SerialName("text_color")
    val textColor: String? = null
)

@Serializable
data class PythonFurniture(
    val name: String,
    val type: String,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    @SerialName("fill_color")
    val fillColor: String,
    @SerialName("outline_color")
    val outlineColor: String
)

@Serializable
data class PythonBehaviorLog(
    @SerialName("student_id")
    val studentId: String,
    val timestamp: String,
    val behavior: String,
    val comment: String
)

@Serializable
data class PythonHomeworkLog(
    @SerialName("student_id")
    val studentId: String,
    val timestamp: String,
    @SerialName("homework_type")
    val homeworkType: String,
    val behavior: String,
    val comment: String,
    @SerialName("homework_details")
    val homeworkDetails: Map<String, String>? = null
)

@Serializable
data class PythonStudentGroup(
    val name: String,
    val color: String
)

@Serializable
data class PythonCustomBehavior(
    val name: String
)

@Serializable
data class PythonCustomHomeworkStatus(
    val name: String
)

@Serializable
data class PythonCustomHomeworkType(
    val name: String
)
