package com.example.myapplication.utils

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.myapplication.ui.dialogs.ExportFilterOptions
import com.example.myapplication.ui.dialogs.ExportType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object ExcelImportUtil {

    @kotlinx.serialization.InternalSerializationApi
    private data class MarksData(
        val markValue: Double?,
        val markType: String?,
        val maxMarkValue: Double?
    )

    private fun getCellStringValue(cell: Cell?): String? {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue?.trim()?.takeIf { it.isNotEmpty() }
            CellType.NUMERIC -> cell.numericCellValue.toString().trim().takeIf { it.isNotEmpty() }
            else -> null
        }
    }

    suspend fun importStudentsFromExcel(
        uri: Uri,
        context: Context,
        studentRepository: StudentRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                if (sheet == null || sheet.physicalNumberOfRows <= 1) {
                    return@withContext Result.failure(Exception("Sheet is empty, contains only a header, or not found"))
                }

                val headerRow = sheet.getRow(0) ?: return@withContext Result.failure(Exception("Header row not found"))

                var firstNameCol = -1
                var lastNameCol = -1

                for (cell in headerRow) {
                    val cellValue = getCellStringValue(cell)?.lowercase()
                    if (cellValue != null) {
                        when {
                            "first name" in cellValue && !"last name".contains(cellValue) -> firstNameCol = cell.columnIndex
                            "last name" in cellValue -> lastNameCol = cell.columnIndex
                        }
                    }
                }

                if (firstNameCol == -1 || lastNameCol == -1) {
                    return@withContext Result.failure(Exception("Required columns (First Name, Last Name) not found or have unexpected content in the Excel header."))
                }

                var importedStudentCount = 0
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue

                    val firstNameCell = row.getCell(firstNameCol)
                    val lastNameCell = row.getCell(lastNameCol)

                    val firstName = getCellStringValue(firstNameCell)
                    val lastName = getCellStringValue(lastNameCell)

                    if (firstName.isNullOrEmpty() || lastName.isNullOrEmpty()) {
                        continue
                    }

                    val student = Student(firstName = firstName, lastName = lastName, xPosition = 0.0F, yPosition = 0.0F)
                    studentRepository.insertStudent(student)
                    importedStudentCount++
                }

                if (importedStudentCount == 0 && sheet.lastRowNum > 0) {
                    return@withContext Result.failure(Exception("No valid student data found in the sheet after the header. All rows were skipped."))
                }
                 if (importedStudentCount == 0 && sheet.lastRowNum == 0) {
                     return@withContext Result.failure(Exception("No student data found below the header row."))
                 }


                Result.success(importedStudentCount)
            } ?: Result.failure(Exception("Failed to open input stream from URI"))
        } catch (e: Exception) {
            Result.failure(Exception("Error importing students: ${e.message}", e))
        }
    }

    @OptIn(kotlinx.serialization.InternalSerializationApi::class)
    suspend fun exportDataToTempFile(
        context: Context,
        students: List<Student>?,
        behaviorLogs: List<BehaviorEvent>?,
        homeworkLogs: List<HomeworkLog>?,
        quizLogs: List<QuizLog>?
    ): java.io.File? = withContext(Dispatchers.IO) {
        try {
            val tempFile = java.io.File.createTempFile("seating_chart_export", ".xlsx", context.cacheDir)
            val uri = Uri.fromFile(tempFile)

            val workbook = XSSFWorkbook()

            students?.let { studentList ->
                val sheet = workbook.createSheet("Students")
                val headerRow = sheet.createRow(0)
                val headers = listOf(
                    "ID", "First Name", "Last Name", "Initials",
                    "X Position", "Y Position", "Custom Width", "Custom Height",
                    "Custom Background Color", "Custom Outline Color", "Custom Text Color"
                )
                headers.forEachIndexed { index, header ->
                    headerRow.createCell(index).setCellValue(header)
                }
                studentList.forEachIndexed { index, student ->
                    val dataRow = sheet.createRow(index + 1)
                    dataRow.createCell(0).setCellValue(student.id.toDouble())
                    dataRow.createCell(1).setCellValue(student.firstName)
                    dataRow.createCell(2).setCellValue(student.lastName)
                    dataRow.createCell(3).setCellValue(student.initials ?: "")
                    dataRow.createCell(4).setCellValue(student.xPosition.toDouble())
                    dataRow.createCell(5).setCellValue(student.yPosition.toDouble())
                    student.customWidth?.let { width -> dataRow.createCell(6).setCellValue(width.toDouble()) } ?: dataRow.createCell(6).setCellValue("")
                    student.customHeight?.let { height -> dataRow.createCell(7).setCellValue(height.toDouble()) } ?: dataRow.createCell(7).setCellValue("")
                    dataRow.createCell(8).setCellValue(student.customBackgroundColor ?: "")
                    dataRow.createCell(9).setCellValue(student.customOutlineColor ?: "")
                    dataRow.createCell(10).setCellValue(student.customTextColor ?: "")
                }
            }

            behaviorLogs?.let { logs ->
                val sheet = workbook.createSheet("Behavior Logs")
                val headerRow = sheet.createRow(0)
                val headers = listOf("ID", "Student Name", "Day", "Type", "Timestamp", "Comment")
                val studentMap = students?.associateBy { it.id }
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }
                logs.forEachIndexed { index, log ->
                    val dataRow = sheet.createRow(index + 1)
                    dataRow.createCell(0).setCellValue(log.id.toDouble())
                    dataRow.createCell(1).setCellValue(studentMap?.get(log.studentId)?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown")
                    dataRow.createCell(2).setCellValue(dayFormat.format(Date(log.timestamp)))
                    dataRow.createCell(3).setCellValue(log.type)
                    dataRow.createCell(4).setCellValue(log.timestamp.toDouble())
                    dataRow.createCell(5).setCellValue(log.comment ?: "")
                }
            }

            homeworkLogs?.let { logs ->
                val sheet = workbook.createSheet("Homework Logs")
                val headerRow = sheet.createRow(0)
                val headers = listOf("ID", "Student ID", "Assignment", "Status", "Logged At", "Comment", "Mark Value", "Mark Type", "Max Mark")
                headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }
                logs.forEachIndexed { index, log ->
                    val dataRow = sheet.createRow(index + 1)
                    dataRow.createCell(0).setCellValue(log.id.toDouble())
                    dataRow.createCell(1).setCellValue(log.studentId.toDouble())
                    dataRow.createCell(2).setCellValue(log.assignmentName)
                    dataRow.createCell(3).setCellValue(log.status)
                    dataRow.createCell(4).setCellValue(log.loggedAt.toDouble())
                    dataRow.createCell(5).setCellValue(log.comment ?: "")
                    log.marksData?.let {
                        val marks = Json.decodeFromString<MarksData>(it)
                        marks.markValue?.let { value -> dataRow.createCell(6).setCellValue(value) } ?: dataRow.createCell(6).setCellValue("")
                        dataRow.createCell(7).setCellValue(marks.markType ?: "")
                        marks.maxMarkValue?.let { value -> dataRow.createCell(8).setCellValue(value) } ?: dataRow.createCell(8).setCellValue("")
                    } ?: run {
                        dataRow.createCell(6).setCellValue("")
                        dataRow.createCell(7).setCellValue("")
                        dataRow.createCell(8).setCellValue("")
                    }
                }
            }

            quizLogs?.let { logs ->
                val sheet = workbook.createSheet("Quiz Logs")
                val headerRow = sheet.createRow(0)
                val headers = listOf("ID", "Student ID", "Quiz Name", "Mark Value", "Mark Type", "Max Mark", "Logged At", "Comment")
                headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }
                logs.forEachIndexed { index, log ->
                    val dataRow = sheet.createRow(index + 1)
                    dataRow.createCell(0).setCellValue(log.id.toDouble())
                    dataRow.createCell(1).setCellValue(log.studentId.toDouble())
                    dataRow.createCell(2).setCellValue(log.quizName)
                    log.markValue?.let { value -> dataRow.createCell(3).setCellValue(value) } ?: dataRow.createCell(3).setCellValue("")
                    dataRow.createCell(4).setCellValue(log.markType ?: "")
                    log.maxMarkValue?.let { value -> dataRow.createCell(5).setCellValue(value) } ?: dataRow.createCell(5).setCellValue("")
                    dataRow.createCell(6).setCellValue(log.loggedAt.toDouble())
                    dataRow.createCell(7).setCellValue(log.comment ?: "")
                }
            }

            java.io.FileOutputStream(tempFile).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            tempFile

        } catch (e: Exception) {
            null
        }
    }

    @OptIn(kotlinx.serialization.InternalSerializationApi::class)
    suspend fun exportData(
        context: Context,
        uri: Uri,
        filterOptions: ExportFilterOptions,
        allStudents: List<Student>,
        behaviorLogs: List<BehaviorEvent>,
        homeworkLogs: List<HomeworkLog>,
        quizLogs: List<QuizLog>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        android.util.Log.d("ExcelExport", "Starting export. Students: ${allStudents.size}, Behaviors: ${behaviorLogs.size}, Homework: ${homeworkLogs.size}, Quizzes: ${quizLogs.size}")
        try {
            val workbook = XSSFWorkbook()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

            val masterLogHeaders = listOf(
                "Student Name", "Day", "Timestamp", "Log Type", "Details", "Comment"
            )

            val studentMap = allStudents.associateBy { it.id }

            fun getStudentFullName(studentId: Long): String {
                val student = studentMap[studentId]
                return "${student?.firstName ?: ""} ${student?.lastName ?: ""}".trim()
            }

            fun addLogEntryToSheet(
                sheet: org.apache.poi.ss.usermodel.Sheet,
                rowIndex: Int,
                studentId: Long,
                logType: String,
                timestamp: Long,
                details: String,
                comment: String?
            ) {
                val row = sheet.createRow(rowIndex)
                row.createCell(0).setCellValue(getStudentFullName(studentId))
                row.createCell(1).setCellValue(dayFormat.format(Date(timestamp)))
                row.createCell(2).setCellValue(dateFormat.format(Date(timestamp)))
                row.createCell(3).setCellValue(logType)
                row.createCell(4).setCellValue(details)
                row.createCell(5).setCellValue(comment ?: "")
            }

            if (filterOptions.exportType == ExportType.MASTER_LOG) {
                val sheet = workbook.createSheet("Master Log")
                val headerRow = sheet.createRow(0)
                masterLogHeaders.forEachIndexed { index, header ->
                    headerRow.createCell(index).setCellValue(header)
                }

                var rowIndex = 1

                if (filterOptions.exportBehaviorLogs) {
                    behaviorLogs.forEach { log ->
                        addLogEntryToSheet(
                            sheet, rowIndex++, log.studentId, "Behavior", log.timestamp,
                            log.type, log.comment
                        )
                    }
                }
                if (filterOptions.exportHomeworkLogs) {
                    homeworkLogs.forEach { log ->
                        val details = "${log.assignmentName} - ${log.status}" +
                                (log.marksData?.let { " (${it})" } ?: "")
                        addLogEntryToSheet(
                            sheet, rowIndex++, log.studentId, "Homework", log.loggedAt,
                            details, log.comment
                        )
                    }
                }
                if (filterOptions.exportQuizLogs) {
                    quizLogs.forEach { log ->
                        val details = "${log.quizName}" +
                                (log.markType?.let { " - ${it}" } ?: "") +
                                (log.markValue?.let { " (${it})" } ?: "") +
                                (log.numQuestions?.let { " / ${it}" } ?: "")
                        addLogEntryToSheet(
                            sheet, rowIndex++, log.studentId, "Quiz", log.loggedAt,
                            details, log.comment
                        )
                    }
                }
            } else { // Individual Student Sheets
                val studentsToExport = allStudents.filter { filterOptions.selectedStudentIds.contains(it.id) }

                studentsToExport.forEach { student ->
                    val sheetName = getStudentFullName(student.id.toLong())
                        .replace(Regex("[\\\\/*?\\[\\]]"), "_") // Replace invalid characters
                        .take(31) // Excel sheet name limit
                    val sheet = workbook.createSheet(sheetName)
                    val headerRow = sheet.createRow(0)
                    masterLogHeaders.forEachIndexed { index, header ->
                        headerRow.createCell(index).setCellValue(header)
                    }

                    var rowIndex = 1

                    if (filterOptions.exportBehaviorLogs) {
                        behaviorLogs.filter { it.studentId == student.id }.forEach { log ->
                            addLogEntryToSheet(
                                sheet, rowIndex++, log.studentId, "Behavior", log.timestamp,
                                log.type, log.comment
                            )
                        }
                    }
                    if (filterOptions.exportHomeworkLogs) {
                        homeworkLogs.filter { it.studentId == student.id }.forEach { log ->
                            val details = "${log.assignmentName} - ${log.status}" +
                                    (log.marksData?.let { " (${it})" } ?: "")
                            addLogEntryToSheet(
                                sheet, rowIndex++, log.studentId, "Homework", log.loggedAt,
                                details, log.comment
                            )
                        }
                    }
                    if (filterOptions.exportQuizLogs) {
                        quizLogs.filter { it.studentId == student.id }.forEach { log ->
                            val details = "${log.quizName}" +
                                    (log.markType?.let { " - ${it}" } ?: "") +
                                    (log.markValue?.let { " (${it})" } ?: "") +
                                    (log.numQuestions?.let { " / ${it}" } ?: "")
                            addLogEntryToSheet(
                                sheet, rowIndex++, log.studentId, "Quiz", log.loggedAt,
                                details, log.comment
                            )
                        }
                    }
                }
            }

            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                android.util.Log.e("ExcelExport", "Failed to open output stream from URI")
                throw Exception("Failed to open output stream from URI")
            }
            outputStream.use {
                workbook.write(it)
            }

            workbook.close()
            android.util.Log.d("ExcelExport", "Export successful")
            Result.success(Unit)

        } catch (e: Exception) {
            android.util.Log.e("ExcelExport", "Error exporting data", e)
            Result.failure(Exception("Error exporting data: ${e.message}", e))
        }
    }
}