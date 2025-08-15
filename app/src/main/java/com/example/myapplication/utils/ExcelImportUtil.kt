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
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object ExcelImportUtil {

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

    suspend fun exportData(
        context: Context, 
        uri: Uri,
        students: List<Student>?,
        behaviorLogs: List<BehaviorEvent>?,
        homeworkLogs: List<HomeworkLog>?,
        quizLogs: List<QuizLog>?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val workbook = XSSFWorkbook()

            students?.let { 
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
                it.forEachIndexed { index, student ->
                    val dataRow = sheet.createRow(index + 1)
                    dataRow.createCell(0).setCellValue(student.id.toDouble()) 
                    dataRow.createCell(1).setCellValue(student.firstName)
                    dataRow.createCell(2).setCellValue(student.lastName)
                    dataRow.createCell(3).setCellValue(student.initials ?: "")
                    dataRow.createCell(4).setCellValue(student.xPosition.toDouble())
                    dataRow.createCell(5).setCellValue(student.yPosition.toDouble())
                    student.customWidth?.let { dataRow.createCell(6).setCellValue(it.toDouble()) } ?: dataRow.createCell(6).setCellValue("")
                    student.customHeight?.let { dataRow.createCell(7).setCellValue(it.toDouble()) } ?: dataRow.createCell(7).setCellValue("")
                    dataRow.createCell(8).setCellValue(student.customBackgroundColor ?: "")
                    dataRow.createCell(9).setCellValue(student.customOutlineColor ?: "")
                    dataRow.createCell(10).setCellValue(student.customTextColor ?: "")
                }
            }

            behaviorLogs?.let {
                val sheet = workbook.createSheet("Behavior Logs")
                val headerRow = sheet.createRow(0)
                val headers = listOf("ID", "Student ID", "Type", "Timestamp", "Comment")
                headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }
                it.forEachIndexed { index, log ->
                    val dataRow = sheet.createRow(index + 1)
                    dataRow.createCell(0).setCellValue(log.id.toDouble())
                    dataRow.createCell(1).setCellValue(log.studentId.toDouble())
                    dataRow.createCell(2).setCellValue(log.type)
                    dataRow.createCell(3).setCellValue(log.timestamp.toDouble())
                    dataRow.createCell(4).setCellValue(log.comment ?: "")
                }
            }

            homeworkLogs?.let {
                val sheet = workbook.createSheet("Homework Logs")
                val headerRow = sheet.createRow(0)
                val headers = listOf("ID", "Student ID", "Assignment", "Status", "Logged At", "Comment", "Mark Value", "Mark Type", "Max Mark")
                headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }
                it.forEachIndexed { index, log ->
                    val dataRow = sheet.createRow(index + 1)
                    dataRow.createCell(0).setCellValue(log.id.toDouble())
                    dataRow.createCell(1).setCellValue(log.studentId.toDouble())
                    dataRow.createCell(2).setCellValue(log.assignmentName)
                    dataRow.createCell(3).setCellValue(log.status)
                    dataRow.createCell(4).setCellValue(log.loggedAt.toDouble())
                    dataRow.createCell(5).setCellValue(log.comment ?: "")
                    log.markValue?.let { dataRow.createCell(6).setCellValue(it.toDouble()) } ?: dataRow.createCell(6).setCellValue("")
                    dataRow.createCell(7).setCellValue(log.markType ?: "")
                    log.maxMarkValue?.let { dataRow.createCell(8).setCellValue(it.toDouble()) } ?: dataRow.createCell(8).setCellValue("")
                }
            }

            quizLogs?.let {
                val sheet = workbook.createSheet("Quiz Logs")
                val headerRow = sheet.createRow(0)
                val headers = listOf("ID", "Student ID", "Quiz Name", "Mark Value", "Mark Type", "Max Mark", "Logged At", "Comment")
                headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }
                it.forEachIndexed { index, log ->
                    val dataRow = sheet.createRow(index + 1)
                    dataRow.createCell(0).setCellValue(log.id.toDouble())
                    dataRow.createCell(1).setCellValue(log.studentId.toDouble())
                    dataRow.createCell(2).setCellValue(log.quizName)
                    log.markValue?.let { dataRow.createCell(3).setCellValue(it.toDouble()) } ?: dataRow.createCell(3).setCellValue("")
                    dataRow.createCell(4).setCellValue(log.markType ?: "")
                    log.maxMarkValue?.let { dataRow.createCell(5).setCellValue(it.toDouble()) } ?: dataRow.createCell(5).setCellValue("")
                    dataRow.createCell(6).setCellValue(log.loggedAt.toDouble())
                    dataRow.createCell(7).setCellValue(log.comment ?: "")
                }
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
            } ?: return@withContext Result.failure(Exception("Failed to open output stream from URI"))
            
            workbook.close()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception("Error exporting data: ${e.message}", e))
        }
    }
}