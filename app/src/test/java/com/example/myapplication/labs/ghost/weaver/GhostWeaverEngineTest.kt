package com.example.myapplication.labs.ghost.weaver

import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.HomeworkLog
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GhostWeaverEngineTest {

    @Test
    fun `identifyThreads correctly identifies academic synergy`() {
        val studentA = 1L
        val studentB = 2L

        val quizLogsByStudent = mapOf(
            studentA to listOf(
                QuizLog(studentId = studentA, quizName = "Math Quiz", markValue = 9.0, maxMarkValue = 10.0, loggedAt = 1000L, marksData = "", numQuestions = 10)
            ),
            studentB to listOf(
                QuizLog(studentId = studentB, quizName = "Math Quiz", markValue = 8.5, maxMarkValue = 10.0, loggedAt = 1001L, marksData = "", numQuestions = 10)
            )
        )

        val threads = GhostWeaverEngine.identifyThreads(quizLogsByStudent, emptyMap())

        assertThat(threads).hasSize(1)
        assertThat(threads[0].type).isEqualTo(GhostWeaverEngine.ThreadType.ACADEMIC_SYNERGY)
        assertThat(threads[0].studentA).isEqualTo(studentA)
        assertThat(threads[0].studentB).isEqualTo(studentB)
        assertThat(threads[0].strength).isGreaterThan(0f)
    }

    @Test
    fun `identifyThreads correctly identifies homework collaboration`() {
        val studentA = 1L
        val studentB = 2L

        val homeworkLogsByStudent = mapOf(
            studentA to listOf(
                HomeworkLog(studentId = studentA, assignmentName = "History Essay", status = "Done", loggedAt = 1000L, marksData = "")
            ),
            studentB to listOf(
                HomeworkLog(studentId = studentB, assignmentName = "History Essay", status = "Complete", loggedAt = 1001L, marksData = "")
            )
        )

        val threads = GhostWeaverEngine.identifyThreads(emptyMap(), homeworkLogsByStudent)

        assertThat(threads).hasSize(1)
        assertThat(threads[0].type).isEqualTo(GhostWeaverEngine.ThreadType.HOMEWORK_COLLABORATION)
        assertThat(threads[0].studentA).isEqualTo(studentA)
        assertThat(threads[0].studentB).isEqualTo(studentB)
    }

    @Test
    fun `identifyThreads ignores low scores and incomplete homework`() {
        val studentA = 1L
        val studentB = 2L

        val quizLogsByStudent = mapOf(
            studentA to listOf(
                QuizLog(studentId = studentA, quizName = "Math Quiz", markValue = 5.0, maxMarkValue = 10.0, loggedAt = 1000L, marksData = "", numQuestions = 10)
            ),
            studentB to listOf(
                QuizLog(studentId = studentB, quizName = "Math Quiz", markValue = 9.0, maxMarkValue = 10.0, loggedAt = 1001L, marksData = "", numQuestions = 10)
            )
        )

        val homeworkLogsByStudent = mapOf(
            studentA to listOf(
                HomeworkLog(studentId = studentA, assignmentName = "History Essay", status = "Not Done", loggedAt = 1000L, marksData = "")
            ),
            studentB to listOf(
                HomeworkLog(studentId = studentB, assignmentName = "History Essay", status = "Done", loggedAt = 1001L, marksData = "")
            )
        )

        val threads = GhostWeaverEngine.identifyThreads(quizLogsByStudent, homeworkLogsByStudent)

        assertThat(threads).isEmpty()
    }
}
