package com.example.myapplication.data

/**
 * A container class for bulk classroom data export.
 *
 * This class aggregates students and their associated historical logs into a single
 * structure, primarily used by the [com.example.myapplication.data.exporter.Exporter]
 * to generate Excel reports or serialized JSON backups.
 *
 * @property students The list of students included in the export.
 * @property behaviorEvents All behavioral incidents for the exported students.
 * @property homeworkLogs All homework completion records for the exported students.
 * @property quizLogs All academic performance records for the exported students.
 */
data class ExportData(
    val students: List<Student>,
    val behaviorEvents: List<BehaviorEvent>,
    val homeworkLogs: List<HomeworkLog>,
    val quizLogs: List<QuizLog>
)
