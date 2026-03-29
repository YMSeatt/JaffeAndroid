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
 *
 * This utility serves as a critical entry point for bulk data ingestion, allowing teachers
 * to import student rosters from standard spreadsheet formats. It is a mobile-optimized
 * port of the logic defined in the Python desktop application, ensuring consistent
 * ingestion behavior across platforms.
 *
 * ### Key Features:
 * 1. **Dynamic Header Detection**: Automatically identifies columns based on a curated set
 *    of aliases (e.g., "surname" or "last" are both mapped to the `last_name` field).
 * 2. **Flexible Name Parsing**: Gracefully handles combined name fields (e.g., "Doe, John"
 *    or "John Doe") as well as split first/last name columns.
 * 3. **Relational Group Assignment**: Automatically links students to their respective
 *    [com.example.myapplication.data.StudentGroup] by matching group names found in the spreadsheet.
 */
object ExcelImportUtil {
    private const val TAG = "ExcelImportUtil"

    /**
     * A mapping of canonical student fields to their common spreadsheet aliases.
     * This map drives the [importStudentsFromExcel] header detection logic, providing
     * resilience against varying CSV/Excel naming conventions.
     */
    private val commonHeaders = mapOf(
        "first_name" to listOf("first", "first name", "firstname"),
        "last_name" to listOf("last", "last name", "lastname", "surname"),
        "full_name" to listOf("full name", "name", "student name"),
        "nickname" to listOf("nickname", "preferred name", "nick"),
        "gender" to listOf("gender", "sex"),
        "group_name" to listOf("group", "group name", "student group")
    )

    /**
     * Imports students from an Excel file URI and persists them to the local database.
     *
     * ### Ingestion Logic & Heuristics:
     *
     * #### 1. Header Resolution
     * The method performs a single pass over the first row to match cell values against
     * [commonHeaders]. Only matched columns are used for data extraction.
     *
     * #### 2. Efficient Group Matching
     * To avoid the **N+1 query problem**, the utility pre-loads all student groups into
     * an in-memory map. Group assignments are resolved via case-insensitive name lookups.
     *
     * #### 3. Name Parsing Heuristics
     * If dedicated first/last name columns are missing, the engine attempts to split the
     * "full name" field using the following priority:
     * - **Comma Delimiter**: Splits "Last, First" into its constituent parts.
     * - **Space Delimiter**: Splits "First Last" at the first space encountered.
     * - **Fallback**: Treats the entire string as the first name if no delimiters are found.
     *
     * #### 4. Data Normalization
     * - **Gender**: Strings containing "girl", "female", or "f" are normalized to "Girl";
     *   all others default to "Boy".
     * - **Coordinates**: New students are initialized at (0, 0). The [CollisionDetector]
     *   will automatically reposition them when they are first added to the seating chart.
     *
     * @param uri The URI of the Excel file (content:// or file://).
     * @param context The application context for content resolution.
     * @param studentRepository The repository for student persistence.
     * @param studentGroupDao The DAO for pre-fetching group identities.
     * @return A [Result] containing the count of successfully imported students.
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

                // Pre-load groups to avoid N+1 query problem
                val groupMap = studentGroupDao.getAllStudentGroupsList().associateBy { it.name.lowercase(Locale.getDefault()) }

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
                            val groupId = groupName?.let { name ->
                                groupMap[name.lowercase(Locale.getDefault())]?.id
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
