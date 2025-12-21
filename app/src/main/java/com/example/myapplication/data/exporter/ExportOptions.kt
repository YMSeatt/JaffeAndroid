package com.example.myapplication.data.exporter

data class ExportOptions(
    val relativeDateRange: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val studentIds: List<Long>? = null,
    val separateSheets: Boolean = false,
    val includeBehaviorLogs: Boolean = true,
    val includeHomeworkLogs: Boolean = true,
    val includeQuizLogs: Boolean = true,
    val includeMasterLog: Boolean = false,
    val includeSummarySheet: Boolean = true,
    val behaviorTypes: List<String>? = null,
    val homeworkTypes: List<String>? = null,
    val includeIndividualStudentSheets: Boolean = false,
    val includeStudentInfoSheet: Boolean = false,
    val encrypt: Boolean = false
)
