package com.example.myapplication.data.importer

import android.content.Context
import android.util.Log
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class Importer(private val context: Context, private val db: AppDatabase) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())

    suspend fun importFromAssets() {
        withContext(Dispatchers.IO) {
            try {
                // Order of import matters to handle relationships
                // These will be implemented later
                // importStudentGroups("student_groups_v10.json")
                // importCustomBehaviors("custom_behaviors_v10.json")
                // importCustomHomeworkTypes("custom_homework_types_v10.json")
                // importCustomHomeworkStatuses("custom_homework_statuses_v10.json")
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
        return db.studentDao().getStudentByStringId(stringId)?.id ?: 0L
    }

    private fun readAssetFile(fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("Importer", "Error reading asset file: $fileName", e)
            null
        }
    }

    suspend fun importData(uri: android.net.Uri) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                if (jsonString != null) {
                    // We need to figure out which type of file this is.
                    // For now, we assume it's a classroom_data file.
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
                            timestamp = dateFormat.parse(logEntry.timestamp)?.time ?: 0L,
                            type = logEntry.behavior,
                            comment = logEntry.comment
                        )
                        behaviorEventDao.insert(behaviorEvent)
                    }
                    "quiz" -> {
                        // Handle quiz log import
                        val quizLogDao = db.quizLogDao()
                        val quizLog = QuizLog(
                             studentId = getStudentDbId(logEntry.studentId),
                             loggedAt = dateFormat.parse(logEntry.timestamp)?.time ?: 0L,
                             quizName = logEntry.behavior, // Assuming behavior is quiz name
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
                    loggedAt = dateFormat.parse(hwLogEntry.timestamp)?.time ?: 0L,
                    assignmentName = hwLogEntry.homeworkType,
                    status = hwLogEntry.homeworkStatus ?: hwLogEntry.behavior,
                    comment = hwLogEntry.comment
                )
                homeworkLogDao.insert(homeworkLog)
            }
            Log.d("Importer", "${classroomData.homeworkLog.size} homework log entries imported.")
        }
    }
}