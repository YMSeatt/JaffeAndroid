package com.example.myapplication.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.di.DatabaseModule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class QuizMarkTypePrepopulationTest {

    private lateinit var db: AppDatabase
    private lateinit var quizMarkTypeDao: QuizMarkTypeDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    runBlocking {
                        db.execSQL("INSERT INTO quiz_mark_types (python_id, name, defaultPoints, contributesToTotal, isExtraCredit) VALUES('mark_correct', 'Correct', 1.0, 1, 0)")
                        db.execSQL("INSERT INTO quiz_mark_types (python_id, name, defaultPoints, contributesToTotal, isExtraCredit) VALUES('mark_incorrect', 'Incorrect', 0.0, 1, 0)")
                        db.execSQL("INSERT INTO quiz_mark_types (python_id, name, defaultPoints, contributesToTotal, isExtraCredit) VALUES('mark_partial', 'Partial Credit', 0.5, 1, 0)")
                        db.execSQL("INSERT INTO quiz_mark_types (python_id, name, defaultPoints, contributesToTotal, isExtraCredit) VALUES('extra_credit', 'Bonus', 1.0, 0, 1)")
                    }
                }
            })
            .build()
        quizMarkTypeDao = db.quizMarkTypeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun `database is pre-populated with default quiz mark types`() = runBlocking {
        val allQuizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
        assertEquals(4, allQuizMarkTypes.size)
        val correctMark = allQuizMarkTypes.find { it.pythonId == "mark_correct" }
        assertNotNull(correctMark)
    }
}
