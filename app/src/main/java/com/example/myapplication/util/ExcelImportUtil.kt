package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentGroupDao
import com.example.myapplication.data.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.util.Locale

/**
 * Utility class for importing student data from Excel files.
 * Ported logic from the Python blueprint to support dynamic headers and flexible name parsing.
 */
object ExcelImportUtil {
    private const val TAG = "ExcelImportUtil"

    private val commonHeaders = mapOf(
        "first_name" to listOf("first", "first name", "firstname"),
        "last_name" to listOf("last", "last name", "lastname", "surname"),
        "full_name" to listOf("full name", "name", "student name"),
        "nickname" to listOf("nickname", "preferred name", "nick"),
        "gender" to listOf("gender", "sex"),
        "group_name" to listOf("group", "group name", "student group")
    )

    /**
     * Imports students from an Excel file URI.
     * Detects headers dynamically and handles various name formats (separate or combined).
     *
     * @param uri The URI of the Excel file.
     * @param context The application context.
     * @param studentRepository The repository to insert students.
     * @param studentGroupDao The DAO to lookup or assign student groups by name.
     * @return A Result containing the number of students successfully imported.
     */
    suspend fun importStudentsFromExcel(
        uri: Uri,
        context: Context,
        studentRepository: StudentRepository,
        studentGroupDao: StudentGroupDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        var importedCount = 0
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream.use { stream ->
                val workbook = WorkbookFactory.create(stream)
                val sheet = workbook.getSheetAt(0)
                val rows = sheet.iterator()

                if (!rows.hasNext()) return@withContext Result.success(0)

                val headerRow = rows.next()
                val formatter = DataFormatter()
                val colIndices = mutableMapOf<String, Int>()

                // Dynamic Header Detection (Ported from Python)
                for (cell in headerRow) {
                    val headerVal = formatter.formatCellValue(cell).lowercase(Locale.getDefault()).trim()
                    for ((key, aliases) in commonHeaders) {
                        if (aliases.contains(headerVal)) {
                            colIndices[key] = cell.columnIndex
                            break
                        }
                    }
                }

                while (rows.hasNext()) {
                    try {
                        val row = rows.next()

                        fun getVal(key: String): String? {
                            return colIndices[key]?.let { idx ->
                                row.getCell(idx)?.let { formatter.formatCellValue(it).trim() }
                            }
                        }

                        var firstName = getVal("first_name") ?: ""
                        var lastName = getVal("last_name") ?: ""
                        val nickname = getVal("nickname") ?: ""
                        val genderStr = getVal("gender") ?: "Boy"
                        val groupName = getVal("group_name")

                        // Combined Name Parsing (Ported from Python)
                        if (firstName.isBlank() || lastName.isBlank()) {
                            val fullNameStr = getVal("full_name")
                            if (!fullNameStr.isNullOrBlank()) {
                                when {
                                    fullNameStr.contains(",") -> {
                                        val parts = fullNameStr.split(",", limit = 2)
                                        lastName = parts[0].trim()
                                        firstName = if (parts.size > 1) parts[1].trim() else ""
                                    }
                                    fullNameStr.contains(" ") -> {
                                        val parts = fullNameStr.split(" ", limit = 2)
                                        firstName = parts[0].trim()
                                        lastName = if (parts.size > 1) parts[1].trim() else ""
                                    }
                                    else -> {
                                        firstName = fullNameStr
                                    }
                                }
                            }
                        }

                        if (firstName.isNotBlank()) {
                            // Find group ID by name if provided (Ported from Python)
                            var groupId: Long? = null
                            if (!groupName.isNullOrBlank()) {
                                try {
                                    val group = studentGroupDao.getGroupByName(groupName)
                                    groupId = group?.id
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error looking up group name: $groupName", e)
                                }
                            }

                            val student = Student(
                                firstName = firstName,
                                lastName = lastName,
                                nickname = nickname.ifBlank { null },
                                gender = if (genderStr.lowercase(Locale.getDefault()) in listOf("girl", "female", "f")) "Girl" else "Boy",
                                stringId = "",
                                xPosition = 0f,
                                yPosition = 0f,
                                groupId = groupId
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
            Log.e(TAG, "Failed to import from Excel", e)
            Result.failure(e)
        }
    }
}
