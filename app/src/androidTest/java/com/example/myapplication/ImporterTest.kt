package com.example.myapplication

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.importer.Importer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ImporterTest {

    private lateinit var db: AppDatabase
    private lateinit var importer: Importer
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        importer = Importer(context, db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testImportData() = runBlocking {
        val json = context.assets.open("sample_data.json").bufferedReader().use { it.readText() }
        val file = File(context.cacheDir, "sample_data.json")
        FileOutputStream(file).use { it.write(json.toByteArray()) }

        importer.importData(Uri.fromFile(file))

        val students = db.studentDao().getAllStudentsNonLiveData()
        assertEquals(1, students.size)
        assertEquals("John", students[0].firstName)

        val behaviorEvents = db.behaviorEventDao().getAllBehaviorEventsList()
        assertEquals(1, behaviorEvents.size)
        assertEquals("Good", behaviorEvents[0].type)
    }
}
