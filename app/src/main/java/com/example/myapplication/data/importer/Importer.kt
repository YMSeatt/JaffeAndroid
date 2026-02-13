package com.example.myapplication.data.importer

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Importer handles the migration and ingestion of classroom data from external sources.
 *
 * It primarily bridges the gap between the Python desktop application (which generates JSON data)
 * and the Android application's Room database. It manages:
 * 1. **Schema Mapping**: Converting Python-style DTOs ([ClassroomDataDto]) into Android entities.
 * 2. **Security**: Handling Fernet-encrypted or plaintext data files automatically.
 * 3. **ID Resolution**: Mapping string-based student IDs from the desktop app to auto-incrementing
 *    Long IDs in the local SQLite database.
 * 4. **Asset & URI Support**: Importing data from bundled assets or user-selected files.
 *
 * @param context Application context.
 * @param db The [AppDatabase] instance for persistence.
 * @param encryptDataFilesFlow A stream indicating whether data files are expected to be encrypted.
 */
class Importer(
    private val context: Context,
    private val db: AppDatabase,
    private val encryptDataFilesFlow: Flow<Boolean>
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    private val securityUtil = SecurityUtil(context)
    private val timestampFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * Converts an ISO-8601 timestamp string into epoch milliseconds.
     * Used to normalize dates between Python's datetime and Android's Long timestamps.
     */
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            LocalDateTime.parse(timestamp, timestampFormatter)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid timestamp format: $timestamp", e)
        }
    }

    /**
     * Triggers a bulk import from a pre-defined asset file (`classroom_data_v10.json`).
     * Primarily used for initial setup or sample data ingestion.
     */
    suspend fun importFromAssets() {
        withContext(Dispatchers.IO) {
            try {
                importClassroomData("classroom_data_v10.json")
                Log.d("Importer", "All data imported successfully.")
            } catch (e: Exception) {
                Log.e("Importer", "Error during import process", e)
            }
        }
    }

    /**
     * Reads and processes a specific JSON file from the application's assets.
     */
    private suspend fun importClassroomData(fileName: String) {
        val jsonString = readAssetFile(fileName) ?: return
        importClassroomDataFromJson(jsonString)
    }

    /**
     * Utility to read asset files, handling decryption automatically if required.
     * Falls back to plaintext if decryption fails, allowing for a mix of secure and insecure sources.
     */
    private suspend fun readAssetFile(fileName: String): String? {
        return try {
            val bytes = context.assets.open(fileName).use { it.readBytes() }

            if (encryptDataFilesFlow.first()) {
                try {
                    securityUtil.decrypt(String(bytes, Charsets.UTF_8))
                } catch (e: Exception) {
                    // If decryption fails, assume it's plaintext
                    String(bytes, Charsets.UTF_8)
                }
            } else {
                String(bytes, Charsets.UTF_8)
            }
        } catch (e: IOException) {
            Log.e("Importer", "Error reading asset file: $fileName", e)
            null
        }
    }

    /**
     * Imports classroom data from a user-provided [android.net.Uri].
     * Handles file I/O and decryption on [Dispatchers.IO].
     */
    suspend fun importData(uri: android.net.Uri) {
        withContext(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }

                if (bytes != null) {
                    val jsonString = if (encryptDataFilesFlow.first()) {
                        try {
                            securityUtil.decrypt(String(bytes, Charsets.UTF_8))
                        } catch (e: Exception) {
                            // If decryption fails, assume it's plaintext
                            String(bytes, Charsets.UTF_8)
                        }
                    } else {
                        String(bytes, Charsets.UTF_8)
                    }
                    importClassroomDataFromJson(jsonString)
                }
            } catch (e: Exception) {
                Log.e("Importer", "Error during import from URI", e)
            }
        }
    }

    /**
     * The core processing logic that maps a deserialized [ClassroomDataDto] into Room entities.
     * This method performs a multi-pass import to ensure referential integrity (e.g. students
     * must be imported before their behavior logs can be linked).
     *
     * Optimized for performance using Room transactions, bulk insertions, and in-memory ID caching.
     */
    private suspend fun importClassroomDataFromJson(jsonString: String) {
        val classroomData = json.decodeFromString<ClassroomDataDto>(jsonString)

        db.withTransaction {
            // Pass 1: Import Students
            val studentList = classroomData.students.values.toList()
            val studentsToInsert = studentList.map { studentDto ->
                Student(
                    stringId = studentDto.id,
                    firstName = studentDto.firstName,
                    lastName = studentDto.lastName,
                    nickname = studentDto.nickname,
                    gender = studentDto.gender,
                    xPosition = studentDto.x.toFloat(),
                    yPosition = studentDto.y.toFloat(),
                    customWidth = studentDto.width.toInt(),
                    customHeight = studentDto.height.toInt()
                )
            }
            val insertedStudentIds = db.studentDao().insertAll(studentsToInsert)
            // Create a mapping from string UUID to local Long ID for O(1) resolution in subsequent passes.
            val studentIdMap = studentList.zip(insertedStudentIds).associate { it.first.id to it.second }
            Log.d("Importer", "${studentsToInsert.size} students imported.")

            // Pass 2: Import Furniture
            val furnitureToInsert = classroomData.furniture.values.map { furnitureDto ->
                Furniture(
                    stringId = furnitureDto.id,
                    name = furnitureDto.name,
                    type = furnitureDto.type,
                    xPosition = furnitureDto.x.toFloat(),
                    yPosition = furnitureDto.y.toFloat(),
                    width = furnitureDto.width.toInt(),
                    height = furnitureDto.height.toInt(),
                    fillColor = furnitureDto.fillColor,
                    outlineColor = furnitureDto.outlineColor
                )
            }
            db.furnitureDao().insertAll(furnitureToInsert)
            Log.d("Importer", "${furnitureToInsert.size} furniture items imported.")

            // Pass 3: Import Behavior and Quiz Logs
            val behaviorEventsToInsert = mutableListOf<BehaviorEvent>()
            val quizLogsToInsert = mutableListOf<QuizLog>()

            classroomData.behaviorLog.forEach { logEntry ->
                val studentId = studentIdMap[logEntry.studentId]
                    ?: throw IllegalArgumentException("Student with stringId ${logEntry.studentId} not found in cache")

                when (logEntry.type) {
                    "behavior" -> {
                        behaviorEventsToInsert.add(BehaviorEvent(
                            studentId = studentId,
                            timestamp = parseTimestamp(logEntry.timestamp),
                            type = logEntry.behavior,
                            comment = logEntry.comment
                        ))
                    }
                    "quiz" -> {
                        quizLogsToInsert.add(QuizLog(
                            studentId = studentId,
                            loggedAt = parseTimestamp(logEntry.timestamp),
                            quizName = logEntry.behavior,
                            comment = logEntry.comment,
                            markValue = logEntry.scoreDetails?.correct?.toDouble(),
                            maxMarkValue = logEntry.scoreDetails?.totalAsked?.toDouble(),
                            markType = null,
                            marksData = "{}",
                            numQuestions = logEntry.scoreDetails?.totalAsked ?: 0
                        ))
                    }
                }
            }
            db.behaviorEventDao().insertAll(behaviorEventsToInsert)
            db.quizLogDao().insertAll(quizLogsToInsert)
            Log.d("Importer", "${behaviorEventsToInsert.size} behavior events and ${quizLogsToInsert.size} quiz logs imported.")

            // Pass 4: Import Homework Logs
            val homeworkLogsToInsert = classroomData.homeworkLog.map { hwLogEntry ->
                val studentId = studentIdMap[hwLogEntry.studentId]
                    ?: throw IllegalArgumentException("Student with stringId ${hwLogEntry.studentId} not found in cache")

                HomeworkLog(
                    studentId = studentId,
                    loggedAt = parseTimestamp(hwLogEntry.timestamp),
                    assignmentName = hwLogEntry.homeworkType ?: "",
                    status = hwLogEntry.homeworkStatus ?: hwLogEntry.behavior,
                    comment = hwLogEntry.comment
                )
            }
            db.homeworkLogDao().insertAll(homeworkLogsToInsert)
            Log.d("Importer", "${homeworkLogsToInsert.size} homework log entries imported.")
        }
    }
}