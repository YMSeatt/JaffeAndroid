package com.example.myapplication.data.exporter

import kotlinx.serialization.Serializable

/**
 * Configuration options for the Excel export process.
 *
 * This data class defines which data sets (students, logs, summaries) are included
 * in the generated report, how the data is filtered (by date, student, or type),
 * and the structural layout of the resulting workbook.
 *
 * @property relativeDateRange A human-readable description of the date range (e.g., "Last 7 Days"). Primarily used for UI display.
 * @property startDate The start of the reporting window in milliseconds since epoch. If null, no lower bound is applied.
 * @property endDate The end of the reporting window in milliseconds since epoch. If null, the current system time is typically used.
 * @property studentIds A list of specific student IDs to include. If null or empty, all students are included.
 * @property separateSheets If true, different log types (Behavior, Quiz, Homework) are placed in individual worksheets.
 * @property includeBehaviorLogs Whether to include behavior-related incident logs in the export.
 * @property includeHomeworkLogs Whether to include homework completion and status logs.
 * @property includeQuizLogs Whether to include academic quiz performance logs.
 * @property includeMasterLog If true, generates a "Master Log" sheet containing all selected log types in a unified chronological list.
 * @property includeSummarySheet Whether to generate an aggregated "Summary" sheet with student-level statistics.
 * @property behaviorTypes Specific behavior categories to filter for (e.g., ["Talking", "Off Task"]). If null, all types are included.
 * @property homeworkTypes Specific homework assignment names to filter for. If null, all assignments are included.
 * @property includeIndividualStudentSheets If true, a dedicated worksheet is created for every student containing their specific history.
 * @property includeStudentInfoSheet Whether to include a reference sheet with student demographic data and group assignments.
 * @property includeAttendanceSheet Whether to generate an "Attendance Report" based on the activity logs found in the selected range.
 * @property encrypt Whether to encrypt the resulting Excel file using the Fernet specification for secure distribution.
 */
@Serializable
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
    val includeAttendanceSheet: Boolean = false,
    val encrypt: Boolean = false
)
