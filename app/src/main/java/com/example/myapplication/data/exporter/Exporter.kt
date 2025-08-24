package com.example.myapplication.data.exporter

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Exporter(private val context: Context) {

    suspend fun exportToXlsx(
        uri: Uri,
        students: List<Student>,
        behaviorLogs: List<BehaviorEvent>,
        homeworkLogs: List<HomeworkLog>,
        quizLogs: List<QuizLog>,
        options: ExportOptions
    ) {
        withContext(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()

                val filteredStudents = students.filter { options.studentIds?.contains(it.id) ?: true }
                val filteredBehaviorLogs = behaviorLogs.filter { options.studentIds?.contains(it.studentId) ?: true }
                val filteredHomeworkLogs = homeworkLogs.filter { options.studentIds?.contains(it.studentId) ?: true }
                val filteredQuizLogs = quizLogs.filter { options.studentIds?.contains(it.studentId) ?: true }


                if (options.separateSheets) {
                    if (options.includeBehaviorLogs) {
                        createSheet(workbook, "Behavior Logs", filteredBehaviorLogs)
                    }
                    if (options.includeHomeworkLogs) {
                        createSheet(workbook, "Homework Logs", filteredHomeworkLogs)
                    }
                    if (options.includeQuizLogs) {
                        createSheet(workbook, "Quiz Logs", filteredQuizLogs)
                    }
                }

                if (options.includeMasterLog || !options.separateSheets) {
                    val allLogs = mutableListOf<Any>()
                    if (options.includeBehaviorLogs) allLogs.addAll(filteredBehaviorLogs)
                    if (options.includeHomeworkLogs) allLogs.addAll(filteredHomeworkLogs)
                    if (options.includeQuizLogs) allLogs.addAll(filteredQuizLogs)
                    createSheet(workbook, "Master Log", allLogs)
                }

                if (options.includeSummarySheet) {
                    createSummarySheet(workbook, filteredStudents, filteredBehaviorLogs, filteredHomeworkLogs, filteredQuizLogs)
                }


                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
        }
    }

    private fun createSummarySheet(
        workbook: XSSFWorkbook,
        students: List<Student>,
        behaviorLogs: List<BehaviorEvent>,
        homeworkLogs: List<HomeworkLog>,
        quizLogs: List<QuizLog>
    ) {
        val sheet = workbook.createSheet("Summary")
        var rowNum = 0

        // Behavior Summary
        val behaviorHeader = sheet.createRow(rowNum++)
        behaviorHeader.createCell(0).setCellValue("Behavior Summary")
        val behaviorCounts = behaviorLogs.groupingBy { it.type }.eachCount()
        behaviorCounts.forEach { (behavior, count) ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(behavior)
            row.createCell(1).setCellValue(count.toDouble())
        }

        rowNum++ // Add a blank row

        // Homework Summary
        val homeworkHeader = sheet.createRow(rowNum++)
        homeworkHeader.createCell(0).setCellValue("Homework Summary")
        val homeworkCounts = homeworkLogs.groupingBy { it.status }.eachCount()
        homeworkCounts.forEach { (status, count) ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(status)
            row.createCell(1).setCellValue(count.toDouble())
        }
    }

    private fun createSheet(workbook: XSSFWorkbook, sheetName: String, data: List<Any>) {
        val sheet = workbook.createSheet(sheetName)
        var rowNum = 0
        val headerRow = sheet.createRow(rowNum++)
        // Create header based on data type
        when (data.firstOrNull()) {
            is BehaviorEvent -> {
                headerRow.createCell(0).setCellValue("Student ID")
                headerRow.createCell(1).setCellValue("Timestamp")
                headerRow.createCell(2).setCellValue("Behavior")
                headerRow.createCell(3).setCellValue("Comment")
            }
            is HomeworkLog -> {
                headerRow.createCell(0).setCellValue("Student ID")
                headerRow.createCell(1).setCellValue("Timestamp")
                headerRow.createCell(2).setCellValue("Type")
                headerRow.createCell(3).setCellValue("Status")
                headerRow.createCell(4).setCellValue("Comment")
            }
            is QuizLog -> {
                headerRow.createCell(0).setCellValue("Student ID")
                headerRow.createCell(1).setCellValue("Timestamp")
                headerRow.createCell(2).setCellValue("Quiz Name")
                headerRow.createCell(3).setCellValue("Score")
                headerRow.createCell(4).setCellValue("Comment")
            }
        }

        data.forEach { item ->
            val row = sheet.createRow(rowNum++)
            when (item) {
                is BehaviorEvent -> {
                    row.createCell(0).setCellValue(item.studentId.toDouble())
                    row.createCell(1).setCellValue(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp)))
                    row.createCell(2).setCellValue(item.type)
                    row.createCell(3).setCellValue(item.comment ?: "")
                }
                is HomeworkLog -> {
                    row.createCell(0).setCellValue(item.studentId.toDouble())
                    row.createCell(1).setCellValue(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(item.loggedAt)))
                    row.createCell(2).setCellValue(item.assignmentName)
                    row.createCell(3).setCellValue(item.status)
                    row.createCell(4).setCellValue(item.comment ?: "")
                }
                is QuizLog -> {
                    row.createCell(0).setCellValue(item.studentId.toDouble())
                    row.createCell(1).setCellValue(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(item.loggedAt)))
                    row.createCell(2).setCellValue(item.quizName)
                    row.createCell(3).setCellValue("${item.markValue}/${item.maxMarkValue}")
                    row.createCell(4).setCellValue(item.comment ?: "")
                }
            }
        }
    }
}