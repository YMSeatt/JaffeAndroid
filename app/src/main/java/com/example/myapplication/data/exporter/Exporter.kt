package com.example.myapplication.data.exporter

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.CustomHomeworkStatus
import com.example.myapplication.data.CustomHomeworkType
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.util.SecurityUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Handles the generation of Excel (.xlsx) reports from application data.
 *
 * This class coordinates the transformation of Room database entities (Students, BehaviorEvents,
 * QuizLogs, HomeworkLogs) into a formatted, multi-sheet Excel workbook using the Apache POI library.
 *
 * ### Workflow:
 * 1. **Filtering**: Processes raw data based on [ExportOptions] (date ranges, student selection, log types).
 * 2. **Workbook Construction**: Initializes an [XSSFWorkbook] and creates specialized styles (bold, alignments, date formats).
 * 3. **Sheet Generation**: Populates various worksheets based on user preferences:
 *    - Log Sheets (Behavior, Quiz, Homework, or Combined).
 *    - Summary Sheets (Aggregated student statistics).
 *    - Individual Student Sheets (Detailed history per student).
 *    - Attendance Reports (Presence tracking based on activity).
 * 4. **Persistence & Security**: Serializes the workbook to a byte array and writes it to a [Uri].
 *    Optionally encrypts the resulting file using [SecurityUtil].
 *
 * @param context The application context, used for accessing files, content resolver, and resources.
 */
class Exporter(
    private val context: Context,
    private val securityUtil: SecurityUtil = SecurityUtil(context)
) {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any>>() {}.type

    /**
     * Cache for decoded marks data JSON strings to avoid redundant deserialization
     * during large export operations involving many log entries.
     */
    private val parsedMarksCache = mutableMapOf<String, Map<String, Any>>()

    /**
     * Helper to safely deserialize JSON-based mark data.
     *
     * @param json The raw JSON string from the database (e.g., [QuizLog.marksData]).
     * @return A map of keys to values, or an empty map if parsing fails.
     */
    private fun parseMarksData(json: String?): Map<String, Any> {
        if (json.isNullOrEmpty()) return emptyMap()
        return parsedMarksCache.getOrPut(json) {
            try {
                gson.fromJson(json, mapType)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    private data class FormattingContext(
        val headerStyle: org.apache.poi.ss.usermodel.CellStyle,
        val rightAlignmentStyle: org.apache.poi.ss.usermodel.CellStyle,
        val leftAlignmentStyle: org.apache.poi.ss.usermodel.CellStyle,
        val fullDateFormat: DateTimeFormatter,
        val dateFormat: DateTimeFormatter,
        val timeFormat: DateTimeFormatter,
        val dayFormat: DateTimeFormatter,
        val attendanceDateFormat: DateTimeFormatter,
        val zoneId: ZoneId
    )

    /**
     * Exports students, behavior events, homework logs, and quiz logs to an Excel file.
     *
     * @param uri The Uri where the Excel file will be written.
     * @param options Configuration for what data to include and how to format it.
     * @param students List of all students.
     * @param behaviorEvents List of behavior events to filter and export.
     * @param homeworkLogs List of homework logs to filter and export.
     * @param quizLogs List of quiz logs to filter and export.
     * @param studentGroups List of student groups for group info sheet.
     * @param quizMarkTypes Configuration for quiz marks (points, total contribution).
     * @param customHomeworkTypes List of user-defined homework categories.
     * @param customHomeworkStatuses List of user-defined homework status labels.
     * @param encrypt Whether to encrypt the resulting Excel file using Fernet.
     */
    suspend fun export(
        uri: Uri,
        options: ExportOptions,
        students: List<Student>,
        behaviorEvents: List<BehaviorEvent>,
        homeworkLogs: List<HomeworkLog>,
        quizLogs: List<QuizLog>,
        studentGroups: List<StudentGroup>,
        quizMarkTypes: List<QuizMarkType>,
        customHomeworkTypes: List<CustomHomeworkType>,
        customHomeworkStatuses: List<CustomHomeworkStatus>,
        encrypt: Boolean
    ) {
        val workbook = XSSFWorkbook()

        val boldFont = workbook.createFont().apply { bold = true }
        val formattingContext = FormattingContext(
            headerStyle = workbook.createCellStyle().apply {
                setFont(boldFont)
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            },
            rightAlignmentStyle = workbook.createCellStyle().apply {
                alignment = HorizontalAlignment.RIGHT
            },
            leftAlignmentStyle = workbook.createCellStyle().apply {
                alignment = HorizontalAlignment.LEFT
            },
            fullDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()),
            timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()),
            dayFormat = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()),
            attendanceDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)", Locale.getDefault()),
            zoneId = ZoneId.systemDefault()
        )

        // Filter data
        // BOLT: Removed redundant O(N) filtering for startDate, endDate and studentIds as data
        // is now pre-filtered at the database level.
        val behaviorTypesSet = options.behaviorTypes?.toSet()
        val homeworkTypesSet = options.homeworkTypes?.toSet()

        val filteredBehaviorEvents = if (behaviorTypesSet == null) {
            behaviorEvents
        } else {
            behaviorEvents.filter { behaviorTypesSet.contains(it.type) }
        }

        val filteredHomeworkLogs = if (homeworkTypesSet == null) {
            homeworkLogs
        } else {
            homeworkLogs.filter { homeworkTypesSet.contains(it.assignmentName) }
        }

        val filteredQuizLogs = quizLogs

        val allLogs = (filteredBehaviorEvents + filteredHomeworkLogs + filteredQuizLogs).sortedBy {
            when (it) {
                is BehaviorEvent -> it.timestamp
                is HomeworkLog -> it.loggedAt
                is QuizLog -> it.loggedAt
                else -> 0
            }
        }
        val studentMap = students.associateBy { it.id }
        val studentGroupMap = studentGroups.associateBy { it.id }


        // Create sheets
        if (options.separateSheets) {
            if (options.includeBehaviorLogs) {
                createSheet(workbook, "Behavior Log", filteredBehaviorEvents, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses, formattingContext)
            }
            if (options.includeQuizLogs) {
                createSheet(workbook, "Quiz Log", filteredQuizLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses, formattingContext)
            }
            if (options.includeHomeworkLogs) {
                createSheet(workbook, "Homework Log", filteredHomeworkLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses, formattingContext)
            }
            if (options.includeMasterLog) {
                createSheet(workbook, "Master Log", allLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses, formattingContext)
            }
        } else {
            createSheet(workbook, "Combined Log", allLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses, formattingContext)
        }

        if (options.includeSummarySheet) {
            createSummarySheet(workbook, allLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses, formattingContext)
        }

        if (options.includeIndividualStudentSheets) {
            createIndividualStudentSheets(workbook, allLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses, formattingContext)
        }

        if (options.includeStudentInfoSheet) {
            createStudentInfoSheet(workbook, students, studentGroupMap)
        }

        if (options.includeAttendanceSheet) {
            createAttendanceSheet(workbook, students, behaviorEvents, homeworkLogs, quizLogs, options, formattingContext)
        }

        // Write to file
        val byteArrayOutputStream = ByteArrayOutputStream()
        workbook.write(byteArrayOutputStream)
        val fileContent = byteArrayOutputStream.toByteArray()
        workbook.close()

        context.contentResolver.openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).use { outputStream ->
                if (encrypt) {
                    val encryptedToken = securityUtil.encrypt(fileContent)
                    outputStream.write(encryptedToken.toByteArray(Charsets.UTF_8))
                } else {
                    outputStream.write(fileContent)
                }
            }
        }
    }

    /**
     * Creates a single worksheet in the given workbook and populates it with log data.
     *
     * This method handles the complex layout of individual log sheets, including:
     * 1. **Dynamic Header Detection**: For homework logs, it scans the provided data set
     *    to identify "ad-hoc" keys in the JSON marks data that aren't defined in the
     *    standard [customHomeworkTypes] list. These are added as additional columns.
     * 2. **Context-Aware Headers**: Adjusts columns based on the sheet type (e.g., adding
     *    "Behavior" for behavior logs vs. "Quiz Name" and scores for quiz logs).
     * 3. **Row Population**: Iterates through log entries, resolving student names and
     *    calculating calculated fields (like percentage scores for quizzes).
     *
     * @param workbook The Apache POI Workbook instance.
     * @param sheetName The name of the sheet to create.
     * @param data The list of log entries (BehaviorEvent, HomeworkLog, or QuizLog).
     * @param students Map of student IDs to Student objects for quick lookup.
     * @param quizMarkTypes Configuration for quiz scoring.
     * @param customHomeworkTypes List of user-defined homework categories.
     * @param customHomeworkStatuses List of user-defined homework status labels.
     * @param context Pre-calculated formatting styles and date formatters.
     */
    private suspend fun createSheet(
        workbook: Workbook,
        sheetName: String,
        data: List<Any>,
        students: Map<Long, Student>,
        quizMarkTypes: List<QuizMarkType>,
        customHomeworkTypes: List<CustomHomeworkType>,
        customHomeworkStatuses: List<CustomHomeworkStatus>,
        context: FormattingContext
    ) {
        val sheet = workbook.createSheet(sheetName)

        sheet.createFreezePane(0, 1)


        val headers = mutableListOf("Timestamp", "Date", "Time", "Day", "First Name", "Last Name")
        val isMasterLog = sheetName == "Master Log" || sheetName == "Combined Log"

        if (isMasterLog) {
            headers.add("Log Type")
        }

        // Collect dynamic keys from Homework Logs if relevant
        // BOLT: Avoid filterIsInstance loop and use a more efficient collection strategy
        val dynamicHomeworkKeys = if (sheetName == "Homework Log" || isMasterLog) {
            val knownHomeworkKeys = (customHomeworkTypes.map { it.name } + customHomeworkStatuses.map { it.name }).toSet()
            val keys = mutableSetOf<String>()
            for (item in data) {
                if (item is HomeworkLog) {
                    item.marksData?.let { json ->
                        keys.addAll(parseMarksData(json).keys)
                    }
                }
            }
            keys.filter { it !in knownHomeworkKeys }.sorted()
        } else {
            emptyList()
        }

        when (sheetName) {
            "Behavior Log" -> headers.add("Behavior")
            "Quiz Log" -> {
                headers.add("Quiz Name")
                headers.add("Num Questions")
                quizMarkTypes.forEach { headers.add(it.name) }
                headers.add("Quiz Score (%)")
            }
            "Homework Log" -> {
                headers.add("Homework Type/Session Name")
                headers.add("Num Items")
                customHomeworkTypes.forEach { headers.add(it.name) }
                customHomeworkStatuses.forEach { headers.add(it.name) }
                dynamicHomeworkKeys.forEach { headers.add(it) } // Add dynamic keys
                headers.add("Homework Score (Total Pts)")
                headers.add("Homework Effort")
            }
            else -> { // Master Log or Combined Log
                headers.add("Item Name")
                quizMarkTypes.forEach { headers.add(it.name) }
                headers.add("Quiz Score (%)")
                customHomeworkTypes.forEach { headers.add(it.name) }
                customHomeworkStatuses.forEach { headers.add(it.name) }
                dynamicHomeworkKeys.forEach { headers.add(it) } // Add dynamic keys
                headers.add("Homework Score (Total Pts)")
                headers.add("Homework Effort")
            }
        }
        headers.add("Comment")

        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(sanitize(header))
            cell.cellStyle = context.headerStyle
        }

        // Cache indices in a Map for O(1) lookup
        val headerIndices = headers.withIndex().associate { it.value to it.index }
        val behaviorCol = headerIndices["Behavior"] ?: -1
        val quizNameCol = headerIndices["Quiz Name"] ?: -1
        val itemNameCol = headerIndices["Item Name"] ?: -1
        val numQuestionsCol = headerIndices["Num Questions"] ?: -1
        val quizScoreCol = headerIndices["Quiz Score (%)"] ?: -1
        val commentCol = headerIndices["Comment"] ?: -1
        val homeworkTypeCol = headerIndices["Homework Type/Session Name"] ?: -1
        val homeworkScoreCol = headerIndices["Homework Score (Total Pts)"] ?: -1
        val homeworkEffortCol = headerIndices["Homework Effort"] ?: -1
        val markTypeIndices = quizMarkTypes.associate { it.name to (headerIndices[it.name] ?: -1) }

        data.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            val studentId = when (item) {
                is BehaviorEvent -> item.studentId
                is HomeworkLog -> item.studentId
                is QuizLog -> item.studentId
                else -> 0L
            }
            val student = students[studentId]

            val timestamp = when (item) {
                is BehaviorEvent -> item.timestamp
                is HomeworkLog -> item.loggedAt
                is QuizLog -> item.loggedAt
                else -> 0L
            }

            val zonedDateTime = Instant.ofEpochMilli(timestamp).atZone(context.zoneId)
            var col = 0
            row.createCell(col++).setCellValue(context.fullDateFormat.format(zonedDateTime))
            row.createCell(col).apply {
                setCellValue(context.dateFormat.format(zonedDateTime))
                cellStyle = context.rightAlignmentStyle
            }
            col++
            row.createCell(col).apply {
                setCellValue(context.timeFormat.format(zonedDateTime))
                cellStyle = context.rightAlignmentStyle
            }
            col++
            row.createCell(col++).setCellValue(context.dayFormat.format(zonedDateTime))
            row.createCell(col++).setCellValue(sanitize(student?.firstName ?: "Unknown"))
            row.createCell(col++).setCellValue(sanitize(student?.lastName ?: ""))

            if (isMasterLog) {
                row.createCell(col++).setCellValue(
                    when (item) {
                        is BehaviorEvent -> "Behavior"
                        is HomeworkLog -> "Homework"
                        is QuizLog -> "Quiz"
                        else -> ""
                    }
                )
            }

            when (item) {
                is BehaviorEvent -> {
                    val targetCol = if (behaviorCol != -1) behaviorCol else itemNameCol
                    if (targetCol != -1) {
                        row.createCell(targetCol).setCellValue(sanitize(item.type))
                    }
                    if (commentCol != -1) {
                        row.createCell(commentCol).apply {
                            setCellValue(sanitize(item.comment ?: ""))
                            cellStyle = context.leftAlignmentStyle
                        }
                    }
                }
                is QuizLog -> {
                    val targetCol = if (quizNameCol != -1) quizNameCol else itemNameCol
                    if (targetCol != -1) {
                        row.createCell(targetCol).setCellValue(sanitize(item.quizName))
                    }
                    if (numQuestionsCol != -1) {
                        row.createCell(numQuestionsCol).apply {
                            setCellValue(item.numQuestions.toDouble())
                            cellStyle = context.rightAlignmentStyle
                        }
                    }

                    val marksData = parseMarksData(item.marksData)

                    var totalScore = 0.0
                    var totalPossible = 0.0

                    quizMarkTypes.forEach { markType ->
                        val rawValue = marksData[markType.name]
                        val markCount = if (rawValue is Number) rawValue.toInt() else rawValue?.toString()?.toIntOrNull() ?: 0
                        val markIndex = markTypeIndices[markType.name] ?: -1
                        if (markIndex != -1) row.createCell(markIndex).apply {
                            setCellValue(markCount.toDouble())
                            cellStyle = context.rightAlignmentStyle
                        }

                        if (markType.contributesToTotal) {
                            totalPossible += item.numQuestions.toDouble() * markType.defaultPoints
                        }
                        totalScore += markCount.toDouble() * markType.defaultPoints
                    }

                    val scorePercent = if (totalPossible > 0) (totalScore / totalPossible) * 100 else 0.0
                    if (quizScoreCol != -1) {
                        row.createCell(quizScoreCol).apply {
                            setCellValue(scorePercent)
                            cellStyle = context.rightAlignmentStyle
                        }
                    }
                    if (commentCol != -1) {
                        row.createCell(commentCol).apply {
                            setCellValue(sanitize(item.comment ?: ""))
                            cellStyle = context.leftAlignmentStyle
                        }
                    }
                }
                is HomeworkLog -> {
                    val targetCol = if (homeworkTypeCol != -1) homeworkTypeCol else itemNameCol
                    if (targetCol != -1) {
                        row.createCell(targetCol).setCellValue(sanitize(item.assignmentName))
                    }
                    val marksData = if (item.marksData != null) parseMarksData(item.marksData) else null

                    if (marksData != null && marksData.isNotEmpty()) {
                        var totalPoints = 0.0
                        var effort = ""
                        
                        // Populate values for all known headers (Custom Types, Statuses, and Dynamic Keys)
                        // We iterate the map entries to find matching headers
                        marksData.forEach { (key, value) ->
                            val index = headerIndices[key] ?: -1
                            if (index != -1) {
                                val stringValue = value.toString()
                                row.createCell(index).apply {
                                    setCellValue(sanitize(stringValue))
                                    cellStyle = context.rightAlignmentStyle
                                }
                                val doubleValue = if (value is Number) value.toDouble() else stringValue.toDoubleOrNull()
                                if (doubleValue != null) {
                                    // Heuristic: only add to total points if it's likely a score? 
                                    // Or maybe we should only count known "Types"?
                                    // For now, let's include it if it's numeric and NOT in Statuses
                                    if (customHomeworkStatuses.none { it.name == key }) {
                                         totalPoints += doubleValue
                                    }
                                }
                                if (key.lowercase(Locale.getDefault()).contains("effort") || 
                                    customHomeworkStatuses.any { it.name == key && it.name.lowercase().contains("effort") }) {
                                    effort = stringValue
                                }
                            }
                        }

                        if (homeworkScoreCol != -1) {
                            row.createCell(homeworkScoreCol).apply {
                                setCellValue(totalPoints)
                                cellStyle = context.rightAlignmentStyle
                            }
                        }
                        if (homeworkEffortCol != -1) {
                            row.createCell(homeworkEffortCol).apply {
                                setCellValue(sanitize(effort))
                                cellStyle = context.rightAlignmentStyle
                            }
                        }
                    }
                    if (commentCol != -1) {
                        row.createCell(commentCol).apply {
                            setCellValue(sanitize(item.comment ?: ""))
                            cellStyle = context.leftAlignmentStyle
                        }
                    }
                }
            }
        }

        // Autosize columns
        // for (i in headers.indices) {
        //     sheet.autoSizeColumn(i)
        // }
    }

    /**
     * Creates a 'Summary' sheet containing aggregated statistics for the entire reporting period.
     *
     * The summary includes:
     * - **Behavior Summary**: Counts of each behavior type per student.
     * - **Quiz Summary**: Average percentage scores and attempt counts for each quiz.
     * - **Homework Summary**: Completion counts and total points earned per student.
     *
     * All summaries are sorted by student last name to ensure a professional report layout.
     *
     * @param workbook The Apache POI Workbook instance.
     * @param data All log entries included in the export.
     * @param students Map of students for name resolution.
     * @param quizMarkTypes Configuration for score calculation.
     * @param customHomeworkTypes Configuration for homework point aggregation.
     * @param customHomeworkStatuses Configuration for homework status resolution.
     * @param context Formatting context.
     */
    private suspend fun createSummarySheet(
        workbook: Workbook,
        data: List<Any>,
        students: Map<Long, Student>,
        quizMarkTypes: List<QuizMarkType>,
        customHomeworkTypes: List<CustomHomeworkType>,
        customHomeworkStatuses: List<CustomHomeworkStatus>,
        context: FormattingContext
    ) {
        val sheet = workbook.createSheet("Summary")
        var currentRow = 0

        // Behavior Summary
        val behaviorLogs = data.filterIsInstance<BehaviorEvent>()
        if (behaviorLogs.isNotEmpty()) {
            val cell = sheet.createRow(currentRow++).createCell(0)
            cell.setCellValue("Behavior Summary by Student")
            cell.cellStyle = context.headerStyle

            val behaviorHeaders = listOf("Student", "Behavior", "Count")
            val headerRow = sheet.createRow(currentRow++)
            behaviorHeaders.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }

            val behaviorCounts = behaviorLogs.groupBy { it.studentId }
                .mapValues { entry -> entry.value.groupingBy { it.type }.eachCount() }

            behaviorCounts.keys.sortedWith(compareBy { students[it]?.lastName }).forEach { studentId ->
                val studentBehaviors = behaviorCounts[studentId]
                studentBehaviors?.keys?.sorted()?.forEach { behavior ->
                    val row = sheet.createRow(currentRow++)
                    val student = students[studentId]
                    row.createCell(0).setCellValue(sanitize(if(student != null) "${student.firstName} ${student.lastName}" else "Unknown"))
                    row.createCell(1).setCellValue(sanitize(behavior))
                    row.createCell(2).setCellValue(studentBehaviors[behavior]?.toDouble() ?: 0.0)
                }
            }
            currentRow++
        }

        // Quiz Summary
        val quizLogs = data.filterIsInstance<QuizLog>()
        if (quizLogs.isNotEmpty()) {
            val cell = sheet.createRow(currentRow++).createCell(0)
            cell.setCellValue("Quiz Averages by Student")
            cell.cellStyle = context.headerStyle

            val quizHeaders = listOf("Student", "Quiz Name", "Avg Score (%)", "Times Taken")
            val headerRow = sheet.createRow(currentRow++)
            quizHeaders.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }

            val quizScores = mutableMapOf<Long, MutableMap<String, MutableList<Double>>>()
            quizLogs.forEach { log ->
                val studentScores = quizScores.getOrPut(log.studentId) { mutableMapOf() }
                val quizScoresList = studentScores.getOrPut(log.quizName) { mutableListOf() }
                // Simplified score calculation
                val marksData = parseMarksData(log.marksData)
                var totalScore = 0.0
                var totalPossible = 0.0
                quizMarkTypes.forEach { markType ->
                    val rawValue = marksData[markType.name]
                    val markCount = if (rawValue is Number) rawValue.toInt() else rawValue?.toString()?.toIntOrNull() ?: 0
                    if (markType.contributesToTotal) {
                        totalPossible += log.numQuestions * markType.defaultPoints
                    }
                    totalScore += markCount * markType.defaultPoints
                }
                val scorePercent = if (totalPossible > 0) (totalScore / totalPossible) * 100 else 0.0
                quizScoresList.add(scorePercent)
            }

            quizScores.keys.sortedWith(compareBy { students[it]?.lastName }).forEach { studentId ->
                val studentQuizzes = quizScores[studentId]
                studentQuizzes?.keys?.sorted()?.forEach { quizName ->
                    val scores = studentQuizzes[quizName]!!
                    val row = sheet.createRow(currentRow++)
                    val student = students[studentId]
                    row.createCell(0).setCellValue(sanitize(if(student != null) "${student.firstName} ${student.lastName}" else "Unknown"))
                    row.createCell(1).setCellValue(sanitize(quizName))
                    row.createCell(2).setCellValue(scores.average())
                    row.createCell(3).setCellValue(scores.size.toDouble())
                }
            }
            currentRow++
        }

        // Homework Summary
        val homeworkLogs = data.filterIsInstance<HomeworkLog>()
        if (homeworkLogs.isNotEmpty()) {
            val cell = sheet.createRow(currentRow++).createCell(0)
            cell.setCellValue("Homework Completion by Student")
            cell.cellStyle = context.headerStyle

            val homeworkHeaders = listOf("Student", "Homework Type/Session", "Count", "Total Points (if applicable)")
            val headerRow = sheet.createRow(currentRow++)
            homeworkHeaders.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }

            val homeworkSummary = mutableMapOf<Long, MutableMap<String, Pair<Int, Double>>>()
            homeworkLogs.forEach { log ->
                val studentSummary = homeworkSummary.getOrPut(log.studentId) { mutableMapOf() }
                val assignmentSummary = studentSummary.getOrPut(log.assignmentName) { Pair(0, 0.0) }
                var points = 0.0
                log.marksData?.let {
                    val marks = parseMarksData(it)
                    marks.values.forEach { value ->
                         val doubleVal = if (value is Number) value.toDouble() else value.toString().toDoubleOrNull()
                        doubleVal?.let { points += it }
                    }
                }
                studentSummary[log.assignmentName] = Pair(assignmentSummary.first + 1, assignmentSummary.second + points)
            }

            homeworkSummary.keys.sortedWith(compareBy { students[it]?.lastName }).forEach { studentId ->
                val studentHomeworks = homeworkSummary[studentId]
                studentHomeworks?.keys?.sorted()?.forEach { assignmentName ->
                    val summary = studentHomeworks[assignmentName]!!
                    val row = sheet.createRow(currentRow++)
                    val student = students[studentId]
                    row.createCell(0).setCellValue(sanitize(if(student != null) "${student.firstName} ${student.lastName}" else "Unknown"))
                    row.createCell(1).setCellValue(sanitize(assignmentName))
                    row.createCell(2).setCellValue(summary.first.toDouble())
                    row.createCell(3).setCellValue(summary.second)
                }
            }
        }

        // for (i in 0..3) {
        //     sheet.autoSizeColumn(i)
        // }
    }

    /**
     * Creates a separate worksheet for each student that has associated log entries.
     *
     * This method is optimized for large classrooms by grouping all provided logs
     * by student ID in a single pass before creating the worksheets. Sheet names
     * are sanitized to comply with Excel's naming restrictions (max 31 characters,
     * restricted special symbols).
     */
    private suspend fun createIndividualStudentSheets(
        workbook: Workbook,
        data: List<Any>,
        students: Map<Long, Student>,
        quizMarkTypes: List<QuizMarkType>,
        customHomeworkTypes: List<CustomHomeworkType>,
        customHomeworkStatuses: List<CustomHomeworkStatus>,
        context: FormattingContext
    ) {
        val logsByStudent = data.groupBy {
            when (it) {
                is BehaviorEvent -> it.studentId
                is HomeworkLog -> it.studentId
                is QuizLog -> it.studentId
                else -> 0L
            }
        }

        logsByStudent.forEach { (studentId, studentLogs) ->
            val student = students[studentId]
            if (student != null) {
                val studentSheetName = "${student.firstName}_${student.lastName}".replace(Regex("[^a-zA-Z0-9_]"), "_").take(31)
                createSheet(workbook, studentSheetName, studentLogs, students, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses, context)
            }
        }
    }

    /**
     * Creates a 'Students Info' sheet with basic demographic and group assignment details.
     */
    private suspend fun createStudentInfoSheet(workbook: Workbook, students: List<Student>, studentGroups: Map<Long, StudentGroup>) {
        val sheet = workbook.createSheet("Students Info")
        val headerRow = sheet.createRow(0)
        val headers = listOf("First Name", "Last Name", "Nickname", "Gender", "Group Name")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(sanitize(header))
        }

        students.forEachIndexed { index, student ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(sanitize(student.firstName))
            row.createCell(1).setCellValue(sanitize(student.lastName))
            row.createCell(2).setCellValue(sanitize(student.nickname ?: ""))
            row.createCell(3).setCellValue(sanitize(student.gender))
            val group = student.groupId?.let { studentGroups[it] }
            row.createCell(4).setCellValue(sanitize(group?.name ?: ""))
        }
    }

    /**
     * Creates an 'Attendance Report' sheet showing student presence based on logged activities.
     * Ported from Python's generate_attendance_data and export_attendance_to_excel.
     *
     * Optimized: Uses a pre-calculated Map of active student-days to achieve O(N) complexity,
     * replacing the original O(S*D*(B+H+Q)) nested loops.
     */
    private fun createAttendanceSheet(
        workbook: Workbook,
        students: List<Student>,
        behaviorEvents: List<BehaviorEvent>,
        homeworkLogs: List<HomeworkLog>,
        quizLogs: List<QuizLog>,
        options: ExportOptions,
        context: FormattingContext
    ) {
        val sheet = workbook.createSheet("Attendance Report")
        sheet.createFreezePane(1, 1)

        val zoneId = context.zoneId

        // Determine date range from options or fallback to log boundaries.
        val startMillis = options.startDate ?: listOfNotNull(
            behaviorEvents.minOfOrNull { it.timestamp },
            homeworkLogs.minOfOrNull { it.loggedAt },
            quizLogs.minOfOrNull { it.loggedAt }
        ).minOrNull() ?: System.currentTimeMillis()
        val endMillis = options.endDate ?: System.currentTimeMillis()

        val reportStartDay = Instant.ofEpochMilli(startMillis).atZone(zoneId).toLocalDate().toEpochDay()
        val reportEndDay = Instant.ofEpochMilli(endMillis).atZone(zoneId).toLocalDate().toEpochDay()

        if (reportEndDay < reportStartDay) return

        // BOLT: Optimize by using epoch days for O(1) presence checks and avoiding Calendar
        val totalDaysInRange = (reportEndDay - reportStartDay + 1).toInt().coerceAtMost(366)
        val lastReportDay = reportStartDay + totalDaysInRange - 1

        // Track active days per student (optimized for O(1) presence checks using epoch day)
        val studentActiveDays = mutableMapOf<Long, MutableSet<Long>>()
        behaviorEvents.forEach { event ->
            studentActiveDays.getOrPut(event.studentId) { mutableSetOf() }.add(
                Instant.ofEpochMilli(event.timestamp).atZone(zoneId).toLocalDate().toEpochDay()
            )
        }
        homeworkLogs.forEach { log ->
            studentActiveDays.getOrPut(log.studentId) { mutableSetOf() }.add(
                Instant.ofEpochMilli(log.loggedAt).atZone(zoneId).toLocalDate().toEpochDay()
            )
        }
        quizLogs.forEach { log ->
            studentActiveDays.getOrPut(log.studentId) { mutableSetOf() }.add(
                Instant.ofEpochMilli(log.loggedAt).atZone(zoneId).toLocalDate().toEpochDay()
            )
        }

        val headers = mutableListOf("Student Name")
        for (day in 0 until totalDaysInRange) {
            val date = LocalDate.ofEpochDay(reportStartDay + day).atStartOfDay(zoneId)
            headers.add(context.attendanceDateFormat.format(date))
        }
        headers.add("Total Present")
        headers.add("Total Absent")

        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(sanitize(header))
            cell.cellStyle = context.headerStyle
        }

        // Optimization: Use a Set for O(1) student ID lookup during student list filtering
        val studentIdsFilterSet = options.studentIds?.toSet()
        val filteredStudents = if (studentIdsFilterSet != null) {
            students.filter { studentIdsFilterSet.contains(it.id) }
        } else {
            students
        }.sortedWith(compareBy({ it.lastName }, { it.firstName }))

        val centerStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
        }

        filteredStudents.forEachIndexed { rowIndex, student ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(sanitize("${student.firstName} ${student.lastName}"))

            var totalPresent = 0
            val presentDays = studentActiveDays[student.id] ?: emptySet()

            for (dayOffset in 0 until totalDaysInRange) {
                val epochDay = reportStartDay + dayOffset
                val isPresent = presentDays.contains(epochDay)

                val cell = row.createCell(dayOffset + 1)
                cell.setCellValue(if (isPresent) "P" else "A")
                cell.cellStyle = centerStyle

                if (isPresent) totalPresent++
            }

            val totalAbsent = totalDaysInRange - totalPresent

            row.createCell(totalDaysInRange + 1).apply {
                setCellValue(totalPresent.toDouble())
                cellStyle = context.rightAlignmentStyle
            }
            row.createCell(totalDaysInRange + 2).apply {
                setCellValue(totalAbsent.toDouble())
                cellStyle = context.rightAlignmentStyle
            }
        }
    }

    /**
     * Sanitizes a string for Excel by prepending a single quote if it starts with
     * a potential formula-triggering character (=, +, -, @).
     *
     * This prevents Excel Formula Injection (CSV Injection) vulnerabilities.
     */
    private fun sanitize(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val triggers = charArrayOf('=', '+', '-', '@')
        return if (value[0] in triggers) {
            "'$value"
        } else {
            value
        }
    }
}
