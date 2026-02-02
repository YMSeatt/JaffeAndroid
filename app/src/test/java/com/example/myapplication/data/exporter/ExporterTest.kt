package com.example.myapplication.data.exporter

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.myapplication.data.*
import com.example.myapplication.util.SecurityUtil
import io.mockk.every
import io.mockk.mockk
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

class ExporterTest {

    @Test
    fun `export Master Log contains Item Name and values for behavior, quiz, and homework`() {
        val context = mockk<Context>(relaxed = true)
        val filesDir = File("build/tmp/testFilesDir")
        filesDir.mkdirs()
        every { context.filesDir } returns filesDir
        every { context.applicationContext } returns context

        val contentResolver = mockk<ContentResolver>(relaxed = true)
        every { context.contentResolver } returns contentResolver

        val uri = mockk<Uri>()
        val tempFile = File("build/tmp/test_export.xlsx")
        if (tempFile.exists()) tempFile.delete()

        val fos = FileOutputStream(tempFile)
        val pfd = mockk<ParcelFileDescriptor>(relaxed = true)
        every { pfd.fileDescriptor } returns fos.fd
        every { contentResolver.openFileDescriptor(uri, "w") } returns pfd

        val securityUtil = SecurityUtil(context)
        val exporter = Exporter(context, securityUtil)

        val students = listOf(Student(id = 1, firstName = "John", lastName = "Doe", stringId = "1"))
        val behaviorEvents = listOf(BehaviorEvent(id = 1, studentId = 1, type = "Participation", timestamp = System.currentTimeMillis(), comment = "test comment"))
        val quizLogs = listOf(QuizLog(id = 1, studentId = 1, quizName = "Math Quiz", markValue = 10.0, maxMarkValue = 10.0, loggedAt = System.currentTimeMillis() + 1000, comment = null, marksData = "{}", numQuestions = 10, markType = "pts"))
        val homeworkLogs = listOf(HomeworkLog(id = 1, studentId = 1, assignmentName = "History HW", status = "Done", loggedAt = System.currentTimeMillis() + 2000))

        val options = ExportOptions(
            includeMasterLog = true,
            separateSheets = false,
            includeBehaviorLogs = true,
            includeQuizLogs = true,
            includeHomeworkLogs = true
        )

        kotlinx.coroutines.runBlocking {
            exporter.export(
                uri = uri,
                options = options,
                students = students,
                behaviorEvents = behaviorEvents,
                homeworkLogs = homeworkLogs,
                quizLogs = quizLogs,
                studentGroups = emptyList(),
                quizMarkTypes = emptyList(),
                customHomeworkTypes = emptyList(),
                customHomeworkStatuses = emptyList(),
                encrypt = false
            )
        }
        fos.close()

        val workbook = XSSFWorkbook(ByteArrayInputStream(tempFile.readBytes()))
        val sheet = workbook.getSheet("Combined Log")
        assertNotNull("Sheet 'Combined Log' should exist", sheet)

        val headerRow = sheet.getRow(0)
        var itemNameCol = -1
        val headers = mutableListOf<String>()
        for (i in 0 until headerRow.lastCellNum.toInt()) {
            val header = headerRow.getCell(i).stringCellValue
            headers.add(header)
            if (header == "Item Name") {
                itemNameCol = i
            }
        }
        println("Headers: $headers")
        println("Item Name Col: $itemNameCol")
        assertTrue("Header 'Item Name' should exist", itemNameCol != -1)

        val itemNames = mutableListOf<String>()
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i)
            val cell = row.getCell(itemNameCol)
            itemNames.add(cell?.stringCellValue ?: "")
        }
        println("Item Names: $itemNames")

        assertTrue("Item Name should contain 'Participation', but got: $itemNames", itemNames.contains("Participation"))
        assertTrue("Item Name should contain 'Math Quiz', but got: $itemNames", itemNames.contains("Math Quiz"))
        assertTrue("Item Name should contain 'History HW', but got: $itemNames", itemNames.contains("History HW"))

        tempFile.delete()
    }

    private fun assertTrue(message: String, condition: Boolean) {
        if (!condition) {
            throw AssertionError(message)
        }
    }

    @Test
    fun `export Quiz Logs with behavior filter should NOT exclude quizzes`() {
        val context = mockk<Context>(relaxed = true)
        val filesDir = File("build/tmp/testFilesDir")
        filesDir.mkdirs()
        every { context.filesDir } returns filesDir
        every { context.applicationContext } returns context

        val contentResolver = mockk<ContentResolver>(relaxed = true)
        every { context.contentResolver } returns contentResolver

        val uri = mockk<Uri>()
        val tempFile = File("build/tmp/test_export_quiz.xlsx")
        if (tempFile.exists()) tempFile.delete()
        val fos = FileOutputStream(tempFile)
        val pfd = mockk<ParcelFileDescriptor>(relaxed = true)
        every { pfd.fileDescriptor } returns fos.fd
        every { contentResolver.openFileDescriptor(uri, "w") } returns pfd

        val securityUtil = SecurityUtil(context)
        val exporter = Exporter(context, securityUtil)

        val students = listOf(Student(id = 1, firstName = "John", lastName = "Doe", stringId = "1"))
        val quizLogs = listOf(QuizLog(id = 1, studentId = 1, quizName = "Math Quiz", markValue = 10.0, maxMarkValue = 10.0, loggedAt = System.currentTimeMillis(), comment = null, marksData = "{}", numQuestions = 10, markType = "pts"))

        val options = ExportOptions(
            includeQuizLogs = true,
            separateSheets = true, // FIX: Need separateSheets = true to get "Quiz Log" sheet
            behaviorTypes = listOf("Good Participation")
        )

        kotlinx.coroutines.runBlocking {
            exporter.export(
                uri = uri,
                options = options,
                students = students,
                behaviorEvents = emptyList(),
                homeworkLogs = emptyList(),
                quizLogs = quizLogs,
                studentGroups = emptyList(),
                quizMarkTypes = emptyList(),
                customHomeworkTypes = emptyList(),
                customHomeworkStatuses = emptyList(),
                encrypt = false
            )
        }
        fos.close()

        val workbook = XSSFWorkbook(ByteArrayInputStream(tempFile.readBytes()))
        val sheet = workbook.getSheet("Quiz Log")

        assertNotNull("Quiz Log sheet should exist", sheet)
        assertEquals("There should be 1 quiz log row (+1 header)", 2, sheet.physicalNumberOfRows)

        tempFile.delete()
    }
}
