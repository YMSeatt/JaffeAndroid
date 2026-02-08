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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Handles the generation of Excel (.xlsx) reports from application data.
 * Supports filtering by date, students, and log types, and can optionally encrypt the output.
 *
 * @param context The application context, used for accessing files and content resolver.
 */
class Exporter(
    private val context: Context
) {

    private val securityUtil = SecurityUtil(context)
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any>>() {}.type

    private data class FormattingContext(
        val headerStyle: org.apache.poi.ss.usermodel.CellStyle,
        val rightAlignmentStyle: org.apache.poi.ss.usermodel.CellStyle,
        val leftAlignmentStyle: org.apache.poi.ss.usermodel.CellStyle,
        val fullDateFormat: SimpleDateFormat,
        val dateFormat: SimpleDateFormat,
        val timeFormat: SimpleDateFormat,
        val dayFormat: SimpleDateFormat
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
            fullDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()),
            dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        )

        // Filter data
        val filteredBehaviorEvents = behaviorEvents.filter { event ->
            (options.startDate == null || event.timestamp >= options.startDate) &&
                    (options.endDate == null || event.timestamp <= options.endDate) &&
                    (options.studentIds == null || options.studentIds.contains(event.studentId)) &&
                    (options.behaviorTypes == null || options.behaviorTypes.contains(event.type))
        }

        val filteredHomeworkLogs = homeworkLogs.filter { log ->
            (options.startDate == null || log.loggedAt >= options.startDate) &&
                    (options.endDate == null || log.loggedAt <= options.endDate) &&
                    (options.studentIds == null || options.studentIds.contains(log.studentId)) &&
                    (options.homeworkTypes == null || options.homeworkTypes.contains(log.assignmentName))
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
     * Handles dynamic headers for Homework logs based on custom types and status.
     *
     * @param workbook The Apache POI Workbook instance.
     * @param sheetName The name of the sheet to create.
     * @param data The list of log entries (BehaviorEvent, HomeworkLog, or QuizLog).
     * @param students Map of student IDs to Student objects for quick lookup.
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
        val dynamicHomeworkKeys = if (sheetName == "Homework Log" || isMasterLog) {
            val relevantLogs = data.filterIsInstance<HomeworkLog>()
            relevantLogs.flatMap { log ->
                 try {
                     log.marksData?.let { 
                        gson.fromJson<Map<String, Any>>(it, mapType).keys
                     } ?: emptySet()
                 } catch (e: Exception) {
                     emptySet()
                 }
            }.distinct().filter { key ->
                // Filter out keys already covered by Custom Types/Statuses
                customHomeworkTypes.none { it.name == key } && customHomeworkStatuses.none { it.name == key }
            }.sorted()
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
            cell.setCellValue(header)
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
                else -> 0
            }

            val date = Date(timestamp)
            var col = 0
            row.createCell(col++).setCellValue(context.fullDateFormat.format(date))
            row.createCell(col).apply {
                setCellValue(context.dateFormat.format(date))
                cellStyle = context.rightAlignmentStyle
            }
            col++
            row.createCell(col).apply {
                setCellValue(context.timeFormat.format(date))
                cellStyle = context.rightAlignmentStyle
            }
            col++
            row.createCell(col++).setCellValue(context.dayFormat.format(date))
            row.createCell(col++).setCellValue(student?.firstName ?: "Unknown")
            row.createCell(col++).setCellValue(student?.lastName ?: "")

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
                        row.createCell(targetCol).setCellValue(item.type)
                    }
                    if (commentCol != -1) {
                        row.createCell(commentCol).apply {
                            setCellValue(item.comment ?: "")
                            cellStyle = context.leftAlignmentStyle
                        }
                    }
                }
                is QuizLog -> {
                    val targetCol = if (quizNameCol != -1) quizNameCol else itemNameCol
                    if (targetCol != -1) {
                        row.createCell(targetCol).setCellValue(item.quizName)
                    }
                    if (numQuestionsCol != -1) {
                        row.createCell(numQuestionsCol).apply {
                            setCellValue(item.numQuestions.toDouble())
                            cellStyle = context.rightAlignmentStyle
                        }
                    }

                    val marksData = try {
                        gson.fromJson<Map<String, Any>>(item.marksData, mapType)
                    } catch (e: Exception) {
                        emptyMap()
                    }

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
                            setCellValue(item.comment ?: "")
                            cellStyle = context.leftAlignmentStyle
                        }
                    }
                }
                is HomeworkLog -> {
                    val targetCol = if (homeworkTypeCol != -1) homeworkTypeCol else itemNameCol
                    if (targetCol != -1) {
                        row.createCell(targetCol).setCellValue(item.assignmentName)
                    }
                    val marksData = try {
                        item.marksData?.let { gson.fromJson<Map<String, Any>>(it, mapType) }
                    } catch (e: Exception) {
                        null
                    }

                    if (marksData != null) {
                        var totalPoints = 0.0
                        var effort = ""
                        
                        // Populate values for all known headers (Custom Types, Statuses, and Dynamic Keys)
                        // We iterate the map entries to find matching headers
                        marksData.forEach { (key, value) ->
                            val index = headerIndices[key] ?: -1
                            if (index != -1) {
                                val stringValue = value.toString()
                                row.createCell(index).apply {
                                    setCellValue(stringValue)
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
                                setCellValue(effort)
                                cellStyle = context.rightAlignmentStyle
                            }
                        }
                    }
                    if (commentCol != -1) {
                        row.createCell(commentCol).apply {
                            setCellValue(item.comment ?: "")
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
     * Creates a 'Summary' sheet containing aggregated statistics:
     * - Behavior counts by student.
     * - Quiz averages and attempt counts by student.
     * - Homework completion counts and total points by student.
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

            behaviorCounts.keys.sortedBy { students[it]?.lastName }.forEach { studentId ->
                val studentBehaviors = behaviorCounts[studentId]
                studentBehaviors?.keys?.sorted()?.forEach { behavior ->
                    val row = sheet.createRow(currentRow++)
                    val student = students[studentId]
                    row.createCell(0).setCellValue(if(student != null) "${student.firstName} ${student.lastName}" else "Unknown")
                    row.createCell(1).setCellValue(behavior)
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
                val marksData = try { 
                    gson.fromJson<Map<String, Any>>(log.marksData, mapType)
                } catch (e: Exception) { emptyMap() }
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

            quizScores.keys.sortedBy { students[it]?.lastName }.forEach { studentId ->
                val studentQuizzes = quizScores[studentId]
                studentQuizzes?.keys?.sorted()?.forEach { quizName ->
                    val scores = studentQuizzes[quizName]!!
                    val row = sheet.createRow(currentRow++)
                    val student = students[studentId]
                    row.createCell(0).setCellValue(if(student != null) "${student.firstName} ${student.lastName}" else "Unknown")
                    row.createCell(1).setCellValue(quizName)
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
                    val marks = try { 
                        gson.fromJson<Map<String, Any>>(it, mapType)
                    } catch (e: Exception) { emptyMap() }
                    marks.values.forEach { value ->
                         val doubleVal = if (value is Number) value.toDouble() else value.toString().toDoubleOrNull()
                        doubleVal?.let { points += it }
                    }
                }
                studentSummary[log.assignmentName] = Pair(assignmentSummary.first + 1, assignmentSummary.second + points)
            }

            homeworkSummary.keys.sortedBy { students[it]?.lastName }.forEach { studentId ->
                val studentHomeworks = homeworkSummary[studentId]
                studentHomeworks?.keys?.sorted()?.forEach { assignmentName ->
                    val summary = studentHomeworks[assignmentName]!!
                    val row = sheet.createRow(currentRow++)
                    val student = students[studentId]
                    row.createCell(0).setCellValue(if(student != null) "${student.firstName} ${student.lastName}" else "Unknown")
                    row.createCell(1).setCellValue(assignmentName)
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
     * Optimized: groups logs by student ID once to avoid O(N*M) filtering.
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
            headerRow.createCell(index).setCellValue(header)
        }

        students.forEachIndexed { index, student ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(student.firstName)
            row.createCell(1).setCellValue(student.lastName)
            row.createCell(2).setCellValue(student.nickname ?: "")
            row.createCell(3).setCellValue(student.gender)
            val group = student.groupId?.let { studentGroups[it] }
            row.createCell(4).setCellValue(group?.name ?: "")
        }
    }

    /**
     * Normalizes a timestamp to the beginning of its day (00:00:00.000).
     * Reuses the provided [Calendar] instance for performance.
     */
    private fun truncateToDay(timestamp: Long, cal: Calendar): Long {
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
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

        val cal = Calendar.getInstance()

        // Determine date range from options or fallback to log boundaries.
        // Optimized: Uses minOfOrNull on individual lists to avoid massive intermediate list allocations.
        val startMillis = options.startDate ?: listOfNotNull(
            behaviorEvents.minOfOrNull { it.timestamp },
            homeworkLogs.minOfOrNull { it.loggedAt },
            quizLogs.minOfOrNull { it.loggedAt }
        ).minOrNull() ?: System.currentTimeMillis()
        val endMillis = options.endDate ?: System.currentTimeMillis()

        val reportStart = truncateToDay(startMillis, cal)
        val reportEnd = truncateToDay(endMillis, cal)

        // Generate list of days in the range
        val dates = mutableListOf<Long>()
        cal.timeInMillis = reportStart
        while (cal.timeInMillis <= reportEnd) {
            // Re-normalizing each day ensures that dateMillis matches the keys in studentActiveDays
            // even if Calendar.add is affected by DST transitions (e.g. midnight becoming 01:00).
            val currentDayStart = truncateToDay(cal.timeInMillis, cal)
            dates.add(currentDayStart)
            cal.timeInMillis = currentDayStart
            cal.add(Calendar.DAY_OF_YEAR, 1)
            if (dates.size > 366) break // Safety break for large ranges
        }

        // Pre-calculate active days for all students to enable O(1) presence checks.
        // This is BOLT's primary optimization for this component.
        // Optimized: Reuses the 'cal' instance to minimize object allocations and memory pressure.
        val studentActiveDays = mutableMapOf<Long, MutableSet<Long>>()

        behaviorEvents.forEach { event ->
            studentActiveDays.getOrPut(event.studentId) { mutableSetOf() }.add(truncateToDay(event.timestamp, cal))
        }
        homeworkLogs.forEach { log ->
            studentActiveDays.getOrPut(log.studentId) { mutableSetOf() }.add(truncateToDay(log.loggedAt, cal))
        }
        quizLogs.forEach { log ->
            studentActiveDays.getOrPut(log.studentId) { mutableSetOf() }.add(truncateToDay(log.loggedAt, cal))
        }

        val headers = mutableListOf("Student Name")
        val attendanceDateFormat = SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.getDefault())
        dates.forEach { headers.add(attendanceDateFormat.format(Date(it))) }
        headers.add("Total Present")
        headers.add("Total Absent")

        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = context.headerStyle
        }

        val filteredStudents = if (options.studentIds != null) {
            students.filter { options.studentIds.contains(it.id) }
        } else {
            students
        }.sortedBy { "${it.lastName} ${it.firstName}" }

        val centerStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
        }

        filteredStudents.forEachIndexed { rowIndex, student ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue("${student.firstName} ${student.lastName}")

            var totalPresent = 0
            var totalAbsent = 0

            val presentDays = studentActiveDays[student.id] ?: emptySet()

            dates.forEachIndexed { dateIndex, dateMillis ->
                // Presence check: O(1) Set lookup
                val isPresent = presentDays.contains(dateMillis)

                val cell = row.createCell(dateIndex + 1)
                cell.setCellValue(if (isPresent) "P" else "A")
                cell.cellStyle = centerStyle

                if (isPresent) totalPresent++ else totalAbsent++
            }

            row.createCell(dates.size + 1).apply {
                setCellValue(totalPresent.toDouble())
                cellStyle = context.rightAlignmentStyle
            }
            row.createCell(dates.size + 2).apply {
                setCellValue(totalAbsent.toDouble())
                cellStyle = context.rightAlignmentStyle
            }
        }
    }
}
