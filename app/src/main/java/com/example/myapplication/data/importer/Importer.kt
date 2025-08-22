package com.example.myapplication.data.importer

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.Student
import com.example.myapplication.preferences.AppPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class Importer(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importData(uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()
                val classroom = json.decodeFromString<Classroom>(jsonString)
                val db = AppDatabase.getDatabase(context)
                val preferencesRepository = AppPreferencesRepository(context)

                // Import students
                val studentDao = db.studentDao()
                val studentIdMap = mutableMapOf<String, Long>()
                classroom.students.values.forEach { importedStudent ->
                    val student = Student(
                        stringId = importedStudent.id,
                        firstName = importedStudent.first_name,
                        lastName = importedStudent.last_name,
                        nickname = importedStudent.nickname,
                        gender = importedStudent.gender,
                        xPosition = importedStudent.x.toFloat(),
                        yPosition = importedStudent.y.toFloat(),
                        customWidth = importedStudent.width.toInt(),
                        customHeight = importedStudent.height.toInt(),
                        customBackgroundColor = importedStudent.style_overrides.fill_color,
                        customOutlineColor = importedStudent.style_overrides.outline_color,
                        customTextColor = importedStudent.style_overrides.font_color
                    )
                    val newId = studentDao.insertStudent(student)
                    studentIdMap[importedStudent.id] = newId
                }

                // Import furniture
                val furnitureDao = db.furnitureDao()
                classroom.furniture.values.forEach { importedFurniture ->
                    val furniture = Furniture(
                        stringId = importedFurniture.id,
                        name = importedFurniture.name,
                        type = importedFurniture.type,
                        xPosition = importedFurniture.x.toFloat(),
                        yPosition = importedFurniture.y.toFloat(),
                        width = importedFurniture.width.toInt(),
                        height = importedFurniture.height.toInt(),
                        fillColor = importedFurniture.fill_color,
                        outlineColor = importedFurniture.outline_color
                    )
                    furnitureDao.insert(furniture)
                }

                // Import behavior logs
                val behaviorEventDao = db.behaviorEventDao()
                classroom.behavior_log.forEach { logEntry ->
                    val studentId = studentIdMap[logEntry.student_id]
                    if (studentId != null) {
                        val timestamp = OffsetDateTime.parse(logEntry.timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli()
                        val behaviorEvent = BehaviorEvent(
                            studentId = studentId,
                            type = logEntry.behavior,
                            timestamp = timestamp,
                            comment = logEntry.comment
                        )
                        behaviorEventDao.insertBehaviorEvent(behaviorEvent)
                    }
                }

                // Import homework logs
                val homeworkLogDao = db.homeworkLogDao()
                classroom.homework_log.forEach { logEntry ->
                    val studentId = studentIdMap[logEntry.student_id]
                    if (studentId != null) {
                        val timestamp = OffsetDateTime.parse(logEntry.timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli()
                        val homeworkLog = HomeworkLog(
                            studentId = studentId,
                            assignmentName = logEntry.homework_type,
                            status = logEntry.homework_status,
                            loggedAt = timestamp,
                            comment = logEntry.comment,
                            marksData = logEntry.marks_data?.let { json.encodeToString(MapSerializer, it) }
                        )
                        homeworkLogDao.insertHomeworkLog(homeworkLog)
                    }
                }

                // Import settings
                preferencesRepository.updatePasswordHash(classroom.settings.app_password_hash ?: "")


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
