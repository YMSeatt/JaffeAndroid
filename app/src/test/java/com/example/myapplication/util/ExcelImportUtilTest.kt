package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.data.StudentGroupDao
import com.example.myapplication.data.StudentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
class ExcelImportUtilTest {

    private lateinit var context: Context
    private val studentRepository: StudentRepository = mockk()
    private val studentGroupDao: StudentGroupDao = mockk()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        coEvery { studentGroupDao.getAllStudentGroupsList() } returns emptyList()
    }

    @Test
    fun testImportWithNumericCellSucceeds() {
        runBlocking {
            coEvery { studentRepository.insertStudent(any()) } returns 1L

            // Create a temporary Excel file with a numeric cell in the first column
            val file = File(context.cacheDir, "test_students_numeric.xlsx")
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Students")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("First Name")
            headerRow.createCell(1).setCellValue("Last Name")

            val dataRow = sheet.createRow(1)
            dataRow.createCell(0).setCellValue(123.0) // Numeric value
            dataRow.createCell(1).setCellValue("Doe")

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            val uri = Uri.fromFile(file)

            val result = ExcelImportUtil.importStudentsFromExcel(uri, context, studentRepository, studentGroupDao)

            assertTrue("Import should now succeed with numeric cells", result.isSuccess)
            assertEquals("One student should have been imported", 1, result.getOrNull())

            val studentSlot = slot<Student>()
            coVerify { studentRepository.insertStudent(capture(studentSlot)) }
            assertEquals("123", studentSlot.captured.firstName)

            file.delete()
        }
    }

    @Test
    fun testImportWithDynamicHeaders() {
        runBlocking {
            coEvery { studentRepository.insertStudent(any()) } returns 1L

            val file = File(context.cacheDir, "test_dynamic_headers.xlsx")
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Students")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("First")
            headerRow.createCell(1).setCellValue("Surname")
            headerRow.createCell(2).setCellValue("Nick")

            val dataRow = sheet.createRow(1)
            dataRow.createCell(0).setCellValue("John")
            dataRow.createCell(1).setCellValue("Doe")
            dataRow.createCell(2).setCellValue("Johnny")

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            val uri = Uri.fromFile(file)
            val result = ExcelImportUtil.importStudentsFromExcel(uri, context, studentRepository, studentGroupDao)

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull())

            val studentSlot = slot<Student>()
            coVerify { studentRepository.insertStudent(capture(studentSlot)) }
            assertEquals("John", studentSlot.captured.firstName)
            assertEquals("Doe", studentSlot.captured.lastName)
            assertEquals("Johnny", studentSlot.captured.nickname)

            file.delete()
        }
    }

    @Test
    fun testImportWithFullNameComma() {
        runBlocking {
            coEvery { studentRepository.insertStudent(any()) } returns 1L

            val file = File(context.cacheDir, "test_full_name_comma.xlsx")
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Students")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Student Name")

            val dataRow = sheet.createRow(1)
            dataRow.createCell(0).setCellValue("Doe, John")

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            val uri = Uri.fromFile(file)
            val result = ExcelImportUtil.importStudentsFromExcel(uri, context, studentRepository, studentGroupDao)

            assertTrue(result.isSuccess)
            val studentSlot = slot<Student>()
            coVerify { studentRepository.insertStudent(capture(studentSlot)) }
            assertEquals("John", studentSlot.captured.firstName)
            assertEquals("Doe", studentSlot.captured.lastName)

            file.delete()
        }
    }

    @Test
    fun testImportWithGroupNameMatching() {
        runBlocking {
            coEvery { studentRepository.insertStudent(any()) } returns 1L
            coEvery { studentGroupDao.getAllStudentGroupsList() } returns listOf(StudentGroup(id = 101, name = "Math Group", color = "#FF0000"))

            val file = File(context.cacheDir, "test_group_matching.xlsx")
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Students")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("First Name")
            headerRow.createCell(1).setCellValue("Last Name")
            headerRow.createCell(2).setCellValue("Group")

            val dataRow = sheet.createRow(1)
            dataRow.createCell(0).setCellValue("Alice")
            dataRow.createCell(1).setCellValue("Smith")
            dataRow.createCell(2).setCellValue("Math Group")

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            val uri = Uri.fromFile(file)
            val result = ExcelImportUtil.importStudentsFromExcel(uri, context, studentRepository, studentGroupDao)

            assertTrue(result.isSuccess)
            val studentSlot = slot<Student>()
            coVerify { studentRepository.insertStudent(capture(studentSlot)) }
            assertEquals("Alice", studentSlot.captured.firstName)
            assertEquals(101L, studentSlot.captured.groupId)

            file.delete()
        }
    }

    @Test
    fun testImportContinuesAfterExceptionInRow() {
        runBlocking {
            coEvery { studentRepository.insertStudent(match { it.firstName == "Fail" }) } throws RuntimeException("DB Error")
            coEvery { studentRepository.insertStudent(match { it.firstName != "Fail" }) } returns 1L

            val file = File(context.cacheDir, "test_students_exception.xlsx")
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Students")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("First Name")
            headerRow.createCell(1).setCellValue("Last Name")

            // Row 1: Valid
            val row1 = sheet.createRow(1)
            row1.createCell(0).setCellValue("John")
            row1.createCell(1).setCellValue("Doe")

            // Row 2: Causes exception
            val row2 = sheet.createRow(2)
            row2.createCell(0).setCellValue("Fail")
            row2.createCell(1).setCellValue("Doe")

            // Row 3: Valid
            val row3 = sheet.createRow(3)
            row3.createCell(0).setCellValue("Alice")
            row3.createCell(1).setCellValue("Smith")

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            val uri = Uri.fromFile(file)
            val result = ExcelImportUtil.importStudentsFromExcel(uri, context, studentRepository, studentGroupDao)

            assertTrue("Import should succeed overall even if one row fails", result.isSuccess)
            assertEquals("Two students should have been successfully imported", 2, result.getOrNull())

            coVerify(exactly = 3) { studentRepository.insertStudent(any()) }

            file.delete()
        }
    }
}
