package com.example.myapplication.ui.dialogs

enum class ExportType {
    MASTER_LOG,
    INDIVIDUAL_STUDENT_SHEETS
}

data class ExportFilterOptions(
    val startDate: Long?,
    val endDate: Long?,
    val exportBehaviorLogs: Boolean,
    val exportHomeworkLogs: Boolean,
    val exportQuizLogs: Boolean,
    val selectedStudentIds: List<Long>,
    val exportType: ExportType,
    val includeSummary: Boolean,
    val separateSheets: Boolean,
    val includeMasterLog: Boolean
)
