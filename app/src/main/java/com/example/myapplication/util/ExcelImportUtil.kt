package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

object ExcelImportUtil {
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

                val formatter = org.apache.poi.ss.usermodel.DataFormatter()
                while (rows.hasNext()) {
                    val row = rows.next()
                    val firstName = formatter.formatCellValue(row.getCell(0))
                    val lastName = formatter.formatCellValue(row.getCell(1))
                    val nickname = formatter.formatCellValue(row.getCell(2))
                    val gender = formatter.formatCellValue(row.getCell(3))

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
                }
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}