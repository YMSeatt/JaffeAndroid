package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

object ExcelImportUtil {
    private const val TAG = "ExcelImportUtil"

    suspend fun importStudentsFromExcel(
        uri: Uri,
        context: Context,
        studentRepository: StudentRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        var importedCount = 0
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream.use { stream ->
                val workbook = WorkbookFactory.create(stream)
                val sheet = workbook.getSheetAt(0)
                val rows = sheet.iterator()

                // Skip header row
                if (rows.hasNext()) {
                    rows.next()
                }

                val formatter = DataFormatter()
                while (rows.hasNext()) {
                    try {
                        val row = rows.next()
                        val firstName = row.getCell(0)?.let { formatter.formatCellValue(it) } ?: ""
                        val lastName = row.getCell(1)?.let { formatter.formatCellValue(it) } ?: ""
                        val nickname = row.getCell(2)?.let { formatter.formatCellValue(it) } ?: ""
                        val gender = row.getCell(3)?.let { formatter.formatCellValue(it) } ?: ""

                        if (firstName.isNotBlank() && lastName.isNotBlank()) {
                            val student = Student(
                                firstName = firstName,
                                lastName = lastName,
                                nickname = nickname,
                                gender = gender,
                                stringId = "",
                                xPosition = 0f,
                                yPosition = 0f
                            )
                            studentRepository.insertStudent(student)
                            importedCount++
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error importing student row", e)
                    }
                }
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}