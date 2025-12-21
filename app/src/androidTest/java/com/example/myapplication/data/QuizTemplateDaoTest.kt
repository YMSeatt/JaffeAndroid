package com.example.myapplication.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizTemplateDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var quizTemplateDao: QuizTemplateDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        quizTemplateDao = database.quizTemplateDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetQuizTemplate() = runBlocking {
        val quizTemplate = QuizTemplate(name = "Test", numQuestions = 10, defaultMarks = emptyMap())
        quizTemplateDao.insert(quizTemplate)
        val allQuizTemplates = quizTemplateDao.getAll().first()
        assertEquals(allQuizTemplates[0], quizTemplate)
    }

    @Test
    fun updateAndGetQuizTemplate() = runBlocking {
        val quizTemplate = QuizTemplate(name = "Test", numQuestions = 10, defaultMarks = emptyMap())
        quizTemplateDao.insert(quizTemplate)
        val updatedQuizTemplate = quizTemplate.copy(name = "Updated Test")
        quizTemplateDao.update(updatedQuizTemplate)
        val allQuizTemplates = quizTemplateDao.getAll().first()
        assertEquals(allQuizTemplates[0], updatedQuizTemplate)
    }

    @Test
    fun deleteAndGetQuizTemplate() = runBlocking {
        val quizTemplate = QuizTemplate(name = "Test", numQuestions = 10, defaultMarks = emptyMap())
        quizTemplateDao.insert(quizTemplate)
        quizTemplateDao.delete(quizTemplate)
        val allQuizTemplates = quizTemplateDao.getAll().first()
        assertEquals(allQuizTemplates.size, 0)
    }
}
