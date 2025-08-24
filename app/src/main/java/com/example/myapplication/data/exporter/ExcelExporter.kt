package com.example.myapplication.data.exporter

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.BehaviorEventDao
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.CustomHomeworkStatusDao
import com.example.myapplication.data.CustomHomeworkTypeDao
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkLogDao
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.QuizLogDao
import com.example.myapplication.data.QuizMarkTypeDao
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroupDao
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream

class ExcelExporter(
    private val context: Context,
    private val studentDao: StudentDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val quizLogDao: QuizLogDao,
    private val studentGroupDao: StudentGroupDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val customHomeworkStatusDao: CustomHomeworkStatusDao,
    private val customHomeworkTypeDao: CustomHomeworkTypeDao,
    private val quizMarkTypeDao: QuizMarkTypeDao
) {

    suspend fun export(uri: Uri, options: ExportOptions) {
        val workbook = XSSFWorkbook()

        // Fetch data from database
        val students = studentDao.getAllStudentsNonLiveData()
        val behaviorEvents = behaviorEventDao.getAllBehaviorEventsList()
        val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
        val quizLogs = quizLogDao.getAllQuizLogsList()

        // Filter data
        val filteredBehaviorEvents = behaviorEvents.filter { event ->
            (options.startDate == null || event.timestamp >= options.startDate) &&
            (options.endDate == null || event.timestamp <= options.endDate) &&
            (options.studentIds == null || options.studentIds.contains(event.studentId))
        }

        val filteredHomeworkLogs = homeworkLogs.filter { log ->
            (options.startDate == null || log.loggedAt >= options.startDate) &&
            (options.endDate == null || log.loggedAt <= options.endDate) &&
            (options.studentIds == null || options.studentIds.contains(log.studentId))
        }

        val filteredQuizLogs = quizLogs.filter { log ->
            (options.startDate == null || log.loggedAt >= options.startDate) &&
            (options.endDate == null || log.loggedAt <= options.endDate) &&
            (options.studentIds == null || options.studentIds.contains(log.studentId))
        }

        val allLogs = (filteredBehaviorEvents + filteredHomeworkLogs + filteredQuizLogs).sortedBy {
            when (it) {
                is BehaviorEvent -> it.timestamp
                is HomeworkLog -> it.loggedAt
                is QuizLog -> it.loggedAt
                else -> 0
            }
        }

        // Create sheets
        if (options.separateSheets) {
            if (options.includeBehaviorLogs) {
                createSheet(workbook, "Behavior Log", filteredBehaviorEvents, students)
            }
            if (options.includeQuizLogs) {
                createSheet(workbook, "Quiz Log", filteredQuizLogs, students)
            }
            if (options.includeHomeworkLogs) {
                createSheet(workbook, "Homework Log", filteredHomeworkLogs, students)
            }
            if (options.includeMasterLog) {
                createSheet(workbook, "Master Log", allLogs, students)
            }
        } else {
            createSheet(workbook, "Combined Log", allLogs, students)
        }

        if (options.includeSummarySheet) {
            createSummarySheet(workbook, allLogs, students)
        }

        if (options.includeIndividualStudentSheets) {
            createIndividualStudentSheets(workbook, allLogs, students)
        }

        if (options.includeStudentInfoSheet) {
            createStudentInfoSheet(workbook, students)
        }


        // Write to file
        context.contentResolver.openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).use { outputStream ->
                workbook.write(outputStream)
            }
        }
        workbook.close()
    }

    private fun createSheet(workbook: Workbook, sheetName: String, data: List<Any>, students: List<Student>) {
        val sheet = workbook.createSheet(sheetName)
        val headerRow = sheet.createRow(0)
        val headers = mutableListOf("Timestamp", "Date", "Time", "Day", "Student ID", "First Name", "Last Name")

        when (sheetName) {
            "Behavior Log" -> headers.add("Behavior")
            "Quiz Log" -> headers.addAll(listOf("Quiz Name", "Num Questions", "Quiz Score (%)"))
            "Homework Log" -> headers.addAll(listOf("Homework Type/Session Name", "Num Items", "Homework Score (Total Pts)", "Homework Effort"))
            "Master Log", "Combined Log" -> {
                headers.add("Log Type")
                headers.add("Item Name")
            }
        }
        headers.add("Comment")

        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        data.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            val student = when (item) {
                is BehaviorEvent -> students.find { it.id == item.studentId }
                is HomeworkLog -> students.find { it.id == item.studentId }
                is QuizLog -> students.find { it.id == item.studentId }
                else -> null
            }

            val timestamp = when (item) {
                is BehaviorEvent -> item.timestamp
                is HomeworkLog -> item.loggedAt
                is QuizLog -> item.loggedAt
                else -> 0
            }

            val date = java.util.Date(timestamp)
            val calendar = java.util.Calendar.getInstance().apply { time = date }

            row.createCell(0).setCellValue(timestamp.toString())
            row.createCell(1).setCellValue(android.text.format.DateFormat.format("yyyy-MM-dd", date).toString())
            row.createCell(2).setCellValue(android.text.format.DateFormat.format("HH:mm:ss", date).toString())
            row.createCell(3).setCellValue(calendar.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.LONG, java.util.Locale.getDefault()))
            row.createCell(4).setCellValue(student?.id?.toString() ?: "")
            row.createCell(5).setCellValue(student?.firstName ?: "")
            row.createCell(6).setCellValue(student?.lastName ?: "")

            var cellIndex = 7
            when (sheetName) {
                "Behavior Log" -> {
                    if (item is BehaviorEvent) {
                        row.createCell(cellIndex++).setCellValue(item.type)
                    }
                }
                "Quiz Log" -> {
                    if (item is QuizLog) {
                        row.createCell(cellIndex++).setCellValue(item.quizName)
                        row.createCell(cellIndex++).setCellValue(item.numQuestions.toString())
                        // Calculate score
                        row.createCell(cellIndex++).setCellValue("") // Placeholder
                    }
                }
                "Homework Log" -> {
                    if (item is HomeworkLog) {
                        row.createCell(cellIndex++).setCellValue(item.assignmentName)
                        row.createCell(cellIndex++).setCellValue("") // Placeholder for num items
                        row.createCell(cellIndex++).setCellValue("") // Placeholder for score
                        row.createCell(cellIndex++).setCellValue("") // Placeholder for effort
                    }
                }
                "Master Log", "Combined Log" -> {
                    when (item) {
                        is BehaviorEvent -> {
                            row.createCell(cellIndex++).setCellValue("Behavior")
                            row.createCell(cellIndex++).setCellValue(item.type)
                        }
                        is QuizLog -> {
                            row.createCell(cellIndex++).setCellValue("Quiz")
                            row.createCell(cellIndex++).setCellValue(item.quizName)
                        }
                        is HomeworkLog -> {
                            row.createCell(cellIndex++).setCellValue("Homework")
                            row.createCell(cellIndex++).setCellValue(item.assignmentName)
                        }
                    }
                }
            }

            val comment = when (item) {
                is BehaviorEvent -> item.comment
                is HomeworkLog -> item.comment
                is QuizLog -> item.comment
                else -> ""
            }
            row.createCell(cellIndex).setCellValue(comment)
        }
    }

    private fun createSummarySheet(workbook: Workbook, data: List<Any>, students: List<Student>) {
        val sheet = workbook.createSheet("Summary")
        // Create summary content
        // ...
    }

    private fun createIndividualStudentSheets(workbook: Workbook, data: List<Any>, students: List<Student>) {
        val studentsWithLogs = data.map {
            when (it) {
                is BehaviorEvent -> it.studentId
                is HomeworkLog -> it.studentId
                is QuizLog -> it.studentId
                else -> 0
            }
        }.distinct()

        studentsWithLogs.forEach { studentId ->
            val student = students.find { it.id == studentId }
            if (student != null) {
                val studentSheet = workbook.createSheet("${student.firstName}_${student.lastName}")
                val studentLogs = data.filter {
                    when (it) {
                        is BehaviorEvent -> it.studentId == studentId
                        is HomeworkLog -> it.studentId == studentId
                        is QuizLog -> it.studentId == studentId
                        else -> false
                    }
                }
                // Populate student sheet
            }
        }
    }

    private fun createStudentInfoSheet(workbook: Workbook, students: List<Student>) {
        val sheet = workbook.createSheet("Students Info")
        val headerRow = sheet.createRow(0)
        val headers = listOf("Student ID", "First Name", "Last Name", "Nickname", "Gender", "Group Name")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        students.forEachIndexed { index, student ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(student.id.toString())
            row.createCell(1).setCellValue(student.firstName)
            row.createCell(2).setCellValue(student.lastName)
            row.createCell(3).setCellValue(student.nickname)
            row.createCell(4).setCellValue(student.gender)
            // Get group name
            row.createCell(5).setCellValue("") // Placeholder
        }
    }
}