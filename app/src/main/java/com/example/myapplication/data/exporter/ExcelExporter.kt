package com.example.myapplication.data.exporter

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

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

        // Fetch all data from database first
        val students = studentDao.getAllStudentsNonLiveData()
        val behaviorEvents = behaviorEventDao.getAllBehaviorEventsList()
        val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
        val quizLogs = quizLogDao.getAllQuizLogsList()
        val studentGroups = studentGroupDao.getAllStudentGroupsList()
        val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
        val customHomeworkTypes = customHomeworkTypeDao.getAllCustomHomeworkTypesList()
        val customHomeworkStatuses = customHomeworkStatusDao.getAllCustomHomeworkStatusesList()

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
                    (options.studentIds == null || options.studentIds.contains(log.studentId)) &&
                    (options.behaviorTypes == null || options.behaviorTypes.contains(log.quizName))
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
                createSheet(workbook, "Behavior Log", filteredBehaviorEvents, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses)
            }
            if (options.includeQuizLogs) {
                createSheet(workbook, "Quiz Log", filteredQuizLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses)
            }
            if (options.includeHomeworkLogs) {
                createSheet(workbook, "Homework Log", filteredHomeworkLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses)
            }
            if (options.includeMasterLog) {
                createSheet(workbook, "Master Log", allLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses)
            }
        } else {
            createSheet(workbook, "Combined Log", allLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses)
        }

        if (options.includeSummarySheet) {
            createSummarySheet(workbook, allLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses)
        }

        if (options.includeIndividualStudentSheets) {
            createIndividualStudentSheets(workbook, allLogs, studentMap, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses)
        }

        if (options.includeStudentInfoSheet) {
            createStudentInfoSheet(workbook, students, studentGroupMap)
        }


        // Write to file
        context.contentResolver.openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).use { outputStream ->
                workbook.write(outputStream)
            }
        }
        workbook.close()
    }

    private suspend fun createSheet(
        workbook: Workbook,
        sheetName: String,
        data: List<Any>,
        students: Map<Long, Student>,
        quizMarkTypes: List<QuizMarkType>,
        customHomeworkTypes: List<CustomHomeworkType>,
        customHomeworkStatuses: List<CustomHomeworkStatus>
    ) {
        val sheet = workbook.createSheet(sheetName)
        val boldFont = workbook.createFont().apply { bold = true }
        val headerStyle = workbook.createCellStyle().apply {
            setFont(boldFont)
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }
        val rightAlignmentStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.RIGHT
        }
        val leftAlignmentStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
        }

        sheet.createFreezePane(0, 1)


        val headers = mutableListOf("Timestamp", "Date", "Time", "Day", "Student Name")
        val isMasterLog = sheetName == "Master Log" || sheetName == "Combined Log"

        if (isMasterLog) {
            headers.add("Log Type")
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
                headers.add("Homework Score (Total Pts)")
                headers.add("Homework Effort")
            }
            else -> { // Master Log or Combined Log
                headers.add("Item Name")
                quizMarkTypes.forEach { headers.add(it.name) }
                headers.add("Quiz Score (%)")
                customHomeworkTypes.forEach { headers.add(it.name) }
                customHomeworkStatuses.forEach { headers.add(it.name) }
                headers.add("Homework Score (Total Pts)")
                headers.add("Homework Effort")
            }
        }
        headers.add("Comment")

        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

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
            row.createCell(col++).setCellValue(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date))
            row.createCell(col).apply {
                setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date))
                cellStyle = rightAlignmentStyle
            }
            col++
            row.createCell(col).apply {
                setCellValue(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date))
                cellStyle = rightAlignmentStyle
            }
            col++
            row.createCell(col++).setCellValue(SimpleDateFormat("EEEE", Locale.getDefault()).format(date))
row.createCell(col++).setCellValue(student?.let { listOf(it.firstName, it.lastName).filter(String::isNotBlank).joinToString(" ") } ?: "Unknown")

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
                    row.createCell(headers.indexOf("Behavior")).setCellValue(item.type)
                    row.createCell(headers.indexOf("Comment")).apply {
                        setCellValue(item.comment ?: "")
                        cellStyle = leftAlignmentStyle
                    }
                }
                is QuizLog -> {
                    row.createCell(headers.indexOf("Quiz Name")).setCellValue(item.quizName)
                    row.createCell(headers.indexOf("Num Questions")).apply {
                        setCellValue(item.numQuestions.toDouble())
                        cellStyle = rightAlignmentStyle
                    }

                    val marksData = try {
                        Json.decodeFromString<Map<String, Int>>(item.marksData)
                    } catch (e: Exception) {
                        emptyMap()
                    }

                    var totalScore = 0.0
                    var totalPossible = 0.0

                    quizMarkTypes.forEach { markType ->
                        val markCount = marksData[markType.id.toString()] ?: 0
                        val markIndex = headers.indexOf(markType.name)
                        if (markIndex != -1) row.createCell(markIndex).apply {
                            setCellValue(markCount.toDouble())
                            cellStyle = rightAlignmentStyle
                        }

                        if (markType.contributesToTotal) {
                            totalPossible += item.numQuestions.toDouble() * markType.defaultPoints
                        }
                        totalScore += markCount.toDouble() * markType.defaultPoints
                    }

                    val scorePercent = if (totalPossible > 0) (totalScore / totalPossible) * 100 else 0.0
                    row.createCell(headers.indexOf("Quiz Score (%)")).apply {
                        setCellValue(scorePercent)
                        cellStyle = rightAlignmentStyle
                    }
                    row.createCell(headers.indexOf("Comment")).apply {
                        setCellValue(item.comment ?: "")
                        cellStyle = leftAlignmentStyle
                    }
                }
                is HomeworkLog -> {
                    row.createCell(headers.indexOf("Homework Type/Session Name")).setCellValue(item.assignmentName)
                    val marksData = try {
                        item.marksData?.let { Json.decodeFromString<Map<String, String>>(it) }
                    } catch (e: Exception) {
                        null
                    }

                    if (marksData != null) {
                        var totalPoints = 0.0
                        var effort = ""
                        customHomeworkTypes.forEach { type ->
                            val value = marksData[type.name]
                            val index = headers.indexOf(type.name)
                            if (index != -1) row.createCell(index).apply {
                                setCellValue(value ?: "")
                                cellStyle = rightAlignmentStyle
                            }
                            if (value != null && value.toDoubleOrNull() != null) {
                                totalPoints += value.toDouble()
                            }
                        }
                        customHomeworkStatuses.forEach { status ->
                            val value = marksData[status.name]
                            val index = headers.indexOf(status.name)
                            if (index != -1) row.createCell(index).apply {
                                setCellValue(value ?: "")
                                cellStyle = rightAlignmentStyle
                            }
                            if (status.name.lowercase(Locale.getDefault()).contains("effort")) {
                                effort = value ?: ""
                            }
                        }
                        row.createCell(headers.indexOf("Homework Score (Total Pts)")).apply {
                            setCellValue(totalPoints)
                            cellStyle = rightAlignmentStyle
                        }
                        row.createCell(headers.indexOf("Homework Effort")).apply {
                            setCellValue(effort)
                            cellStyle = rightAlignmentStyle
                        }
                    }
                    row.createCell(headers.indexOf("Comment")).apply {
                        setCellValue(item.comment ?: "")
                        cellStyle = leftAlignmentStyle
                    }
                }
            }
        }

        // Autosize columns
        for (i in headers.indices) {
            sheet.autoSizeColumn(i)
        }
    }

    private suspend fun createSummarySheet(
        workbook: Workbook,
        data: List<Any>,
        students: Map<Long, Student>,
        quizMarkTypes: List<QuizMarkType>,
        customHomeworkTypes: List<CustomHomeworkType>,
        customHomeworkStatuses: List<CustomHomeworkStatus>
    ) {
        val sheet = workbook.createSheet("Summary")
        val boldFont = workbook.createFont().apply { bold = true }
        var currentRow = 0

        // Behavior Summary
        val behaviorLogs = data.filterIsInstance<BehaviorEvent>()
        if (behaviorLogs.isNotEmpty()) {
            val cell = sheet.createRow(currentRow++).createCell(0)
            cell.setCellValue("Behavior Summary by Student")
            cell.cellStyle = workbook.createCellStyle().apply { setFont(boldFont) }

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
            cell.cellStyle = workbook.createCellStyle().apply { setFont(boldFont) }

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
                val marksData = try { Json.decodeFromString<Map<String, Int>>(log.marksData) } catch (e: Exception) { emptyMap() }
                var totalScore = 0.0
                var totalPossible = 0.0
                quizMarkTypes.forEach { markType ->
                    val markCount = marksData[markType.id.toString()] ?: 0
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
            cell.cellStyle = workbook.createCellStyle().apply { setFont(boldFont) }

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
                    val marks = try { Json.decodeFromString<Map<String, String>>(it) } catch (e: Exception) { emptyMap() }
                    marks.values.forEach { value ->
                        value.toDoubleOrNull()?.let { points += it }
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

        for (i in 0..3) {
            sheet.autoSizeColumn(i)
        }
    }

    private suspend fun createIndividualStudentSheets(
        workbook: Workbook,
        data: List<Any>,
        students: Map<Long, Student>,
        quizMarkTypes: List<QuizMarkType>,
        customHomeworkTypes: List<CustomHomeworkType>,
        customHomeworkStatuses: List<CustomHomeworkStatus>
    ) {
        val studentsWithLogs = data.map {
            when (it) {
                is BehaviorEvent -> it.studentId
                is HomeworkLog -> it.studentId
                is QuizLog -> it.studentId
                else -> 0
            }
        }.distinct()

        studentsWithLogs.forEach { studentId ->
            val student = students[studentId]
            if (student != null) {
                val studentSheetName = "${student.firstName}_${student.lastName}".replace(Regex("[^a-zA-Z0-9_]"), "_").take(31)
                val studentLogs = data.filter {
                    when (it) {
                        is BehaviorEvent -> it.studentId == studentId
                        is HomeworkLog -> it.studentId == studentId
                        is QuizLog -> it.studentId == studentId
                        else -> false
                    }
                }
                createSheet(workbook, studentSheetName, studentLogs, students, quizMarkTypes, customHomeworkTypes, customHomeworkStatuses)
            }
        }
    }

    private suspend fun createStudentInfoSheet(workbook: Workbook, students: List<Student>, studentGroups: Map<Long, StudentGroup>) {
        val sheet = workbook.createSheet("Students Info")
        val headerRow = sheet.createRow(0)
        val headers = listOf("Student Name", "Nickname", "Gender", "Group Name")
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        students.forEachIndexed { index, student ->
            val row = sheet.createRow(index + 1)
row.createCell(0).setCellValue(listOf(student.firstName, student.lastName).filter(String::isNotBlank).joinToString(" "))
            row.createCell(1).setCellValue(student.nickname ?: "")
            row.createCell(2).setCellValue(student.gender)
            val group = student.groupId?.let { studentGroups[it] }
            row.createCell(3).setCellValue(group?.name ?: "")
        }
    }
}