package com.example.myapplication.labs.ghost.tiles

import android.content.Context
import com.example.myapplication.data.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class GhostTileLogicTest {
    @Test
    fun testGetLastActiveStudentId() = runBlocking {
        val behaviorDao = mockk<BehaviorEventDao>()
        val studentDao = mockk<StudentDao>()
        val homeworkDao = mockk<HomeworkLogDao>()
        val quizDao = mockk<QuizLogDao>()
        val furnitureDao = mockk<FurnitureDao>()
        val layoutDao = mockk<LayoutTemplateDao>()
        val quizMarkDao = mockk<QuizMarkTypeDao>()
        val context = mockk<Context>()

        val repository = StudentRepository(
            studentDao, behaviorDao, homeworkDao, quizDao, furnitureDao, layoutDao, quizMarkDao, context
        )

        val lastEvent = BehaviorEvent(
            id = 1,
            studentId = 42,
            type = "Positive Participation",
            comment = "Test",
            timestamp = 1000L
        )

        coEvery { behaviorDao.getLastBehaviorEvent() } returns lastEvent

        val lastId = repository.getLastActiveStudentId()
        assertEquals(42L, lastId)

        coVerify { behaviorDao.getLastBehaviorEvent() }
    }
}
