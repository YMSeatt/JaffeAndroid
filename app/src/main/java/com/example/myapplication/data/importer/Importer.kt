package com.example.myapplication.data.importer

import android.content.Context
import android.util.Log
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import com.example.myapplication.utils.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

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

    private fun parseTimestamp(timestamp: String): Long {
        // Handle different fractional second formats
        val trimmedTimestamp = if (timestamp.contains(".")) {
            val parts = timestamp.split(".")
            val wholePart = parts[0]
            val fractionalPart = parts[1].take(6).padEnd(6, '0')
            "$wholePart.$fractionalPart"
        } else {
            timestamp
        }
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        return format.parse(trimmedTimestamp)?.time ?: throw IllegalArgumentException("Invalid timestamp format: $timestamp")
    }

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

    private suspend fun importClassroomData(fileName: String) {
        val jsonString = readAssetFile(fileName) ?: return
        importClassroomDataFromJson(jsonString)
    }

    private suspend fun getStudentDbId(stringId: String): Long {
        return db.studentDao().getStudentByStringId(stringId)?.id
            ?: throw IllegalArgumentException("Student with stringId $stringId not found")
    }

    private suspend fun readAssetFile(fileName: String): String? {
        return try {
            val bytes = context.assets.open(fileName).use { it.readBytes() }

            if (encryptDataFilesFlow.first()) {
                try {
                    SecurityUtil.decrypt(String(bytes))
                } catch (e: Exception) {
                    // If decryption fails, assume it's plaintext
                    String(bytes)
                }
            } else {
                String(bytes)
            }
        } catch (e: IOException) {
            Log.e("Importer", "Error reading asset file: $fileName", e)
            null
        }
    }

    suspend fun importData(uri: android.net.Uri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val jsonString = if (encryptDataFilesFlow.first()) {
                        try {
                            SecurityUtil.decrypt(String(bytes))
                        } catch (e: Exception) {
                            // If decryption fails, assume it's plaintext
                            String(bytes)
                        }
                    } else {
                        String(bytes)
                    }
                    importClassroomDataFromJson(jsonString)
                }
            } catch (e: Exception) {
                Log.e("Importer", "Error during import from URI", e)
            }
        }
    }

    private suspend fun importClassroomDataFromJson(jsonString: String) {
        val classroomData = json.decodeFromString<ClassroomDataDto>(jsonString)

        // Import Students
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

        // Import Furniture
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

        // Import Behavior Log
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

        // Import Homework Log
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