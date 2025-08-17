package com.example.myapplication.util

import android.content.Context
import android.os.Environment
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.StudentDetailsForDisplay
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

suspend fun exportToExcel(context: Context, students: List<StudentDetailsForDisplay>, behaviorEvents: List<BehaviorEvent>): Boolean {
    val workbook: Workbook = XSSFWorkbook()

    // Sheet 1: Student List
    val studentsSheet = workbook.createSheet("Students")
    var rowNum = 0
    val studentHeaderRow = studentsSheet.createRow(rowNum++)
    studentHeaderRow.createCell(0).setCellValue("ID")
    studentHeaderRow.createCell(1).setCellValue("First Name")
    studentHeaderRow.createCell(2).setCellValue("Last Name")
    // Removed Seat Number header

    students.forEach {
        val row = studentsSheet.createRow(rowNum++)
        row.createCell(0).setCellValue(it.id.toString())
        row.createCell(1).setCellValue(it.firstName)
        row.createCell(2).setCellValue(it.lastName)
        // Removed seatNumber data
    }

    // Sheet 2: Behavior Log
    val behaviorSheet = workbook.createSheet("Behavior Log")
    rowNum = 0
    val behaviorHeaderRow = behaviorSheet.createRow(rowNum++)
    behaviorHeaderRow.createCell(0).setCellValue("Event ID")
    behaviorHeaderRow.createCell(1).setCellValue("Student ID")
    behaviorHeaderRow.createCell(2).setCellValue("Student Name")
    behaviorHeaderRow.createCell(3).setCellValue("Behavior Type")
    behaviorHeaderRow.createCell(4).setCellValue("Timestamp")
    behaviorHeaderRow.createCell(5).setCellValue("Comment")

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    behaviorEvents.forEach { event ->
        val student = students.find { it.id.toLong() == event.studentId }
        val row = behaviorSheet.createRow(rowNum++)
        row.createCell(0).setCellValue(event.id.toString())
        row.createCell(1).setCellValue(event.studentId.toString())
        row.createCell(2).setCellValue(student?.let { "${it.firstName} ${it.lastName}" } ?: "N/A")
        row.createCell(3).setCellValue(event.type)
        row.createCell(4).setCellValue(sdf.format(Date(event.timestamp)))
        row.createCell(5).setCellValue(event.comment ?: "")
    }

    try {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "SeatingChartData_${System.currentTimeMillis()}.xlsx")
        val fileOut = FileOutputStream(file)
        workbook.write(fileOut)
        fileOut.close()
        workbook.close()
        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}
