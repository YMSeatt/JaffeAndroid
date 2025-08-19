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
import java.util.TimeZone
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
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val masterLogHeaders = listOf(
                "Student Name", "Day", "Timestamp", "Log Type", "Details", "Comment"
            )

            val studentMap = allStudents.associateBy { it.id }

            fun getStudentFullName(studentId: Long): String {
                val student = studentMap[studentId]
                return "${student?.firstName.orEmpty()} ${student?.lastName.orEmpty()}".trim()
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
                try {
                    val row = sheet.createRow(rowIndex)
                    row.createCell(0).setCellValue(getStudentFullName(studentId))
                    row.createCell(1).setCellValue(dayFormat.format(Date(timestamp)))
                    row.createCell(2).setCellValue(dateFormat.format(Date(timestamp)))
                    row.createCell(3).setCellValue(logType)
                    row.createCell(4).setCellValue(details)
                    row.createCell(5).setCellValue(comment ?: "")
                } catch (e: Exception) {
                    android.util.Log.e("ExcelExport", "Error adding log entry to sheet", e)
                }
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
                                (log.marksData?.let { " ($it)" } ?: "")
                        addLogEntryToSheet(
                            sheet, rowIndex++, log.studentId, "Homework", log.loggedAt,
                            details, log.comment
                        )
                    }
                }
                if (filterOptions.exportQuizLogs) {
                    quizLogs.forEach { log ->
                        val details = "${log.quizName}" +
                                (log.markType?.let { " - $it" } ?: "") +
                                (log.markValue?.let { " ($it)" } ?: "") +
                                (log.numQuestions?.let { " / $it" } ?: "")
                        addLogEntryToSheet(
                            sheet, rowIndex++, log.studentId, "Quiz", log.loggedAt,
                            details, log.comment
                        )
                    }
                }
            } else { // Individual Student Sheets
                val studentsToExport = allStudents.filter { filterOptions.selectedStudentIds.contains(it.id) }

                studentsToExport.forEach { student ->
                    val sheetName = getStudentFullName(student.id)
                        .replace(Regex("[\\/*?[\\]]"), "_") // Replace invalid characters
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
                                    (log.marksData?.let { " ($it)" } ?: "")
                            addLogEntryToSheet(
                                sheet, rowIndex++, log.studentId, "Homework", log.loggedAt,
                                details, log.comment
                            )
                        }
                    }
                    if (filterOptions.exportQuizLogs) {
                        quizLogs.filter { it.studentId == student.id }.forEach { log ->
                            val details = "${log.quizName}" +
                                    (log.markType?.let { " - $it" } ?: "") +
                                    (log.markValue?.let { " ($it)" } ?: "") +
                                    (log.numQuestions?.let { " / $it" } ?: "")
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
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Export successful", android.widget.Toast.LENGTH_SHORT).show()
            }
            Result.success(Unit)

        } catch (e: Exception) {
            android.util.Log.e("ExcelExport", "Error exporting data", e)
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Error exporting data: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
            Result.failure(Exception("Error exporting data: ${e.message}", e))
        }
    }
}