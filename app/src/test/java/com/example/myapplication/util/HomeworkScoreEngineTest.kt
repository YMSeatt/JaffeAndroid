package com.example.myapplication.util

import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkMarkMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeworkScoreEngineTest {

    private val metadata = listOf(
        HomeworkMarkMetadata(id = 1, name = "Complete", defaultPoints = 10.0),
        HomeworkMarkMetadata(id = 2, name = "Partial", defaultPoints = 5.0),
        HomeworkMarkMetadata(id = 3, name = "Late", defaultPoints = -2.0)
    )

    @Test
    fun `calculateTotalPoints with numeric marks`() {
        val log = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "N/A",
            marksData = "{\"Score\": 8.5, \"Bonus\": 1.0}"
        )
        val score = HomeworkScoreEngine.calculateTotalPoints(log, metadata)
        assertEquals(9.5, score, 0.001)
    }

    @Test
    fun `calculateTotalPoints with status mapping`() {
        val log = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "Complete",
            marksData = "{}"
        )
        val score = HomeworkScoreEngine.calculateTotalPoints(log, metadata)
        assertEquals(10.0, score, 0.001)
    }

    @Test
    fun `calculateTotalPoints with Yes status mapping to Complete`() {
        val log = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "Yes",
            marksData = "{}"
        )
        val score = HomeworkScoreEngine.calculateTotalPoints(log, metadata)
        assertEquals(10.0, score, 0.001)
    }

    @Test
    fun `calculateTotalPoints with mixed numeric and status`() {
        val log = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "Partial",
            marksData = "{\"Bonus\": 2.0}"
        )
        val score = HomeworkScoreEngine.calculateTotalPoints(log, metadata)
        assertEquals(7.0, score, 0.001)
    }

    @Test
    fun `calculateTotalPoints with negative points from status`() {
        val log = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "Late",
            marksData = "{\"Score\": 10.0}"
        )
        val score = HomeworkScoreEngine.calculateTotalPoints(log, metadata)
        assertEquals(8.0, score, 0.001)
    }

    @Test
    fun `calculateTotalPoints with unknown status`() {
        val log = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "Unknown",
            marksData = "{\"Score\": 5.0}"
        )
        val score = HomeworkScoreEngine.calculateTotalPoints(log, metadata)
        assertEquals(5.0, score, 0.001)
    }

    @Test
    fun `calculateTotalPoints with empty marksData and empty status`() {
        val log = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "",
            marksData = ""
        )
        val score = HomeworkScoreEngine.calculateTotalPoints(log, metadata)
        assertEquals(0.0, score, 0.001)
    }
}
