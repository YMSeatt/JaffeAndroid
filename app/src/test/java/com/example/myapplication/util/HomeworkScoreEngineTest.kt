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

    @Test
    fun `calculateTotalPoints Python blueprint parity cases`() {
        val testMetadata = listOf(
            HomeworkMarkMetadata(id = 1, name = "Complete", defaultPoints = 10.0),
            HomeworkMarkMetadata(id = 2, name = "Late", defaultPoints = 5.0),
            HomeworkMarkMetadata(id = 3, name = "Bonus", defaultPoints = 2.0)
        )

        // Case 1: Yes status (mapping to Complete = 10.0) + Reading = 1.0 -> 11.0
        val log1 = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "Yes",
            marksData = "{\"Reading\": 1.0}"
        )
        assertEquals(11.0, HomeworkScoreEngine.calculateTotalPoints(log1, testMetadata), 0.001)

        // Case 2: Late status (5.0) + empty marks -> 5.0
        val log2 = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "Late",
            marksData = "{}"
        )
        assertEquals(5.0, HomeworkScoreEngine.calculateTotalPoints(log2, testMetadata), 0.001)

        // Case 3: No status + Bonus: "Yes" (maps to Bonus metadata = 2.0) + Math = 5.0 -> 7.0
        val log3 = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "No",
            marksData = "{\"Bonus\": \"Yes\", \"Math\": 5.0}"
        )
        assertEquals(7.0, HomeworkScoreEngine.calculateTotalPoints(log3, testMetadata), 0.001)

        // Case 4: Complete status (10.0) + Extra = 5.0 -> 15.0
        val log4 = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "Complete",
            marksData = "{\"Extra\": 5.0}"
        )
        assertEquals(15.0, HomeworkScoreEngine.calculateTotalPoints(log4, testMetadata), 0.001)
    }

    @Test
    fun `calculateTotalPoints Python boolean evaluation parity`() {
        val testMetadata = listOf(
            HomeworkMarkMetadata(id = 1, name = "Bonus", defaultPoints = 2.0)
        )

        // Booleans are numeric in Python (True = 1.0, False = 0.0).
        // Under Python logic, {"Bonus": True} counts as numeric, adding 1.0 but NOT triggering status-like key mapping.
        val logTrue = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "No",
            marksData = "{\"Bonus\": true}"
        )
        assertEquals(1.0, HomeworkScoreEngine.calculateTotalPoints(logTrue, testMetadata), 0.001)

        val logFalse = HomeworkLog(
            studentId = 1,
            assignmentName = "Math",
            status = "No",
            marksData = "{\"Bonus\": false}"
        )
        assertEquals(0.0, HomeworkScoreEngine.calculateTotalPoints(logFalse, testMetadata), 0.001)
    }
}
