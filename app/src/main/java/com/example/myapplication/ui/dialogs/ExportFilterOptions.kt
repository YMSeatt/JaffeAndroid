package com.example.myapplication.ui.dialogs

enum class ExportType {
    MASTER_LOG,
    INDIVIDUAL_STUDENTS,
    INDIVIDUAL_STUDENT_SHEETS
}

data class ExportFilterOptions(
    val exportType: ExportType,
    val selectedStudentIds: List<Long>,
    val exportBehaviorLogs: Boolean,
    val exportHomeworkLogs: Boolean,
    val exportQuizLogs: Boolean,
    val startDate: Long?,
    val endDate: Long?
)
