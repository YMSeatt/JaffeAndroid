package com.example.myapplication.data.importer

import android.content.Context
import android.util.Log
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
     * Look up the internal Long database ID for a student using their string-based UUID.
     */
    private suspend fun getStudentDbId(stringId: String): Long {
        return db.studentDao().getStudentByStringId(stringId)?.id
            ?: throw IllegalArgumentException("Student with stringId $stringId not found")
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
     */
    private suspend fun importClassroomDataFromJson(jsonString: String) {
        val classroomData = json.decodeFromString<ClassroomDataDto>(jsonString)

        // Pass 1: Import Students
        // We use stringId to maintain a link to the original Python-generated identifier.
        val studentDao = db.studentDao()
        classroomData.students.values.forEach { studentDto ->
            val student = Student(
                stringId = studentDto.id,
                firstName = studentDto.firstName,
                lastName = studentDto.lastName,
                nickname = studentDto.nickname,
                gender = studentDto.gender,
                xPosition = studentDto.x.toFloat(),
                yPosition = studentDto.y.toFloat(),
                customWidth = studentDto.width.toInt(),
                customHeight = studentDto.height.toInt()
                // groupId will be handled later if needed
            )
            studentDao.insert(student)
        }
        Log.d("Importer", "${classroomData.students.size} students imported.")

        // Pass 2: Import Furniture
        val furnitureDao = db.furnitureDao()
        classroomData.furniture.values.forEach { furnitureDto ->
            val furniture = Furniture(
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
            furnitureDao.insert(furniture)
        }
        Log.d("Importer", "${classroomData.furniture.size} furniture items imported.")

        // Pass 3: Import Behavior and Quiz Logs
        // Python combines these in behaviorLog; we split them based on the 'type' field.
        withContext(Dispatchers.IO) {
            val behaviorEventDao = db.behaviorEventDao()
            classroomData.behaviorLog.forEach { logEntry ->
                when (logEntry.type) {
                    "behavior" -> {
                        val behaviorEvent = BehaviorEvent(
                            studentId = getStudentDbId(logEntry.studentId),
                            timestamp = parseTimestamp(logEntry.timestamp),
                            type = logEntry.behavior,
                            comment = logEntry.comment
                        )
                        behaviorEventDao.insert(behaviorEvent)
                    }
                    "quiz" -> {
                        val quizLogDao = db.quizLogDao()
                        val quizLog = QuizLog(
                            studentId = getStudentDbId(logEntry.studentId),
                            loggedAt = parseTimestamp(logEntry.timestamp),
                            quizName = logEntry.behavior,
                            comment = logEntry.comment,
                            markValue = logEntry.scoreDetails?.correct?.toDouble(),
                            maxMarkValue = logEntry.scoreDetails?.totalAsked?.toDouble(),
                            markType = null,
                            marksData = "{}",
                            numQuestions = logEntry.scoreDetails?.totalAsked ?: 0
                        )
                        quizLogDao.insert(quizLog)
                    }
                }
            }
            Log.d("Importer", "${classroomData.behaviorLog.size} behavior/quiz log entries imported.")
        }

        // Pass 4: Import Homework Logs
        withContext(Dispatchers.IO) {
            val homeworkLogDao = db.homeworkLogDao()
            classroomData.homeworkLog.forEach { hwLogEntry ->
                val homeworkLog = HomeworkLog(
                    studentId = getStudentDbId(hwLogEntry.studentId),
                    loggedAt = parseTimestamp(hwLogEntry.timestamp),
                    assignmentName = hwLogEntry.homeworkType ?: "",
                    status = hwLogEntry.homeworkStatus ?: hwLogEntry.behavior,
                    comment = hwLogEntry.comment
                )
                homeworkLogDao.insert(homeworkLog)
            }
            Log.d("Importer", "${classroomData.homeworkLog.size} homework log entries imported.")
        }
    }
}