package com.example.myapplication.data.importer

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.BehaviorEventDao
import com.example.myapplication.data.CustomBehavior
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.CustomHomeworkStatus
import com.example.myapplication.data.CustomHomeworkStatusDao
import com.example.myapplication.data.CustomHomeworkType
import com.example.myapplication.data.CustomHomeworkTypeDao
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.FurnitureDao
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkLogDao
import com.example.myapplication.data.HomeworkMarkStep
import com.example.myapplication.data.HomeworkMarkType
import com.example.myapplication.data.HomeworkTemplate
import com.example.myapplication.data.HomeworkTemplateDao
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.data.StudentGroupDao
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.ZoneOffset

class JsonImporter(
    private val context: Context,
    private val studentDao: StudentDao,
    private val furnitureDao: FurnitureDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val studentGroupDao: StudentGroupDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val customHomeworkStatusDao: CustomHomeworkStatusDao,
    private val customHomeworkTypeDao: CustomHomeworkTypeDao,
    private val homeworkTemplateDao: HomeworkTemplateDao
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun importData(
        classroomDataUri: Uri,
        studentGroupsUri: Uri,
        customBehaviorsUri: Uri,
        customHomeworkStatusesUri: Uri,
        customHomeworkTypesUri: Uri,
        homeworkTemplatesUri: Uri? = null
    ) {
        importStudentGroups(studentGroupsUri)
        importClassroomData(classroomDataUri)
        importCustomBehaviors(customBehaviorsUri)
        importCustomHomeworkStatuses(customHomeworkStatusesUri)
        importCustomHomeworkTypes(customHomeworkTypesUri)
        if (homeworkTemplatesUri != null) {
            importHomeworkTemplates(homeworkTemplatesUri)
        }
    }

    private suspend fun importHomeworkTemplates(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<List<PythonHomeworkTemplate>>(content)
        pythonData.forEach { pythonTemplate ->
            val steps = pythonTemplate.steps.map { pythonStep ->
                HomeworkMarkStep(
                    label = pythonStep.label,
                    type = when (pythonStep.type.lowercase()) {
                        "checkbutton", "checkbox" -> HomeworkMarkType.CHECKBOX
                        "scale", "slider", "score" -> HomeworkMarkType.SCORE
                        "entry", "text", "comment" -> HomeworkMarkType.COMMENT
                        else -> HomeworkMarkType.COMMENT // Fallback
                    },
                    maxValue = pythonStep.maxValue
                )
            }
            val homeworkTemplate = HomeworkTemplate(
                name = pythonTemplate.name,
                marksData = json.encodeToString(steps)
            )
            homeworkTemplateDao.insert(homeworkTemplate)
        }
    }

    private fun readFileContent(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.readText()
    }

    private suspend fun importClassroomData(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<PythonClassroomData>(content)

        // Import students
        pythonData.students.forEach { (stringId, pythonStudent) ->
            val student = Student(
                stringId = stringId,
                firstName = pythonStudent.firstName,
                lastName = pythonStudent.lastName,
                nickname = pythonStudent.nickname,
                gender = pythonStudent.gender,
                xPosition = pythonStudent.x.toFloat(),
                yPosition = pythonStudent.y.toFloat(),
                customWidth = pythonStudent.styleOverrides.width?.toInt(),
                customHeight = pythonStudent.styleOverrides.height?.toInt(),
                customBackgroundColor = pythonStudent.styleOverrides.fillColor,
                customOutlineColor = pythonStudent.styleOverrides.outlineColor,
                customTextColor = pythonStudent.styleOverrides.textColor,
                groupId = studentGroupDao.getGroupByName(pythonStudent.groupId)?.id
            )
            studentDao.insert(student)
        }

        // Import furniture
        pythonData.furniture.forEach { (stringId, pythonFurniture) ->
            val furniture = Furniture(
                stringId = stringId,
                name = pythonFurniture.name,
                type = pythonFurniture.type,
                xPosition = pythonFurniture.x.toFloat(),
                yPosition = pythonFurniture.y.toFloat(),
                width = pythonFurniture.width.toInt(),
                height = pythonFurniture.height.toInt(),
                fillColor = pythonFurniture.fillColor,
                outlineColor = pythonFurniture.outlineColor
            )
            furnitureDao.insert(furniture)
        }

        // Import behavior logs
        pythonData.behaviorLog.forEach { pythonBehaviorLog ->
            val student = studentDao.getStudentByStringId(pythonBehaviorLog.studentId)
            if (student != null) {
                val behaviorEvent = BehaviorEvent(
                    studentId = student.id,
                    type = pythonBehaviorLog.behavior,
                    timestamp = LocalDateTime.parse(pythonBehaviorLog.timestamp)
                        .toEpochSecond(ZoneOffset.UTC),
                    comment = pythonBehaviorLog.comment
                )
                behaviorEventDao.insert(behaviorEvent)
            }
        }

        // Import homework logs
        pythonData.homeworkLog.forEach { pythonHomeworkLog ->
            val student = studentDao.getStudentByStringId(pythonHomeworkLog.studentId)
            if (student != null) {
                val homeworkLog = HomeworkLog(
                    studentId = student.id,
                    assignmentName = pythonHomeworkLog.homeworkType,
                    status = pythonHomeworkLog.behavior,
                    loggedAt = LocalDateTime.parse(pythonHomeworkLog.timestamp)
                        .toEpochSecond(ZoneOffset.UTC),
                    comment = pythonHomeworkLog.comment,
                    marksData = pythonHomeworkLog.homeworkDetails?.let { json.encodeToString(it) }
                )
                homeworkLogDao.insert(homeworkLog)
            }
        }
    }

    private suspend fun importStudentGroups(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<Map<String, PythonStudentGroup>>(content)
        pythonData.values.forEach { pythonStudentGroup ->
            val studentGroup = StudentGroup(
                name = pythonStudentGroup.name,
                color = pythonStudentGroup.color
            )
            studentGroupDao.insert(studentGroup)
        }
    }

    private suspend fun importCustomBehaviors(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<List<PythonCustomBehavior>>(content)
        pythonData.forEach { pythonCustomBehavior ->
            val customBehavior = CustomBehavior(
                name = pythonCustomBehavior.name
            )
            customBehaviorDao.insert(customBehavior)
        }
    }

    private suspend fun importCustomHomeworkStatuses(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<List<PythonCustomHomeworkStatus>>(content)
        pythonData.forEach { pythonCustomHomeworkStatus ->
            val customHomeworkStatus = CustomHomeworkStatus(
                name = pythonCustomHomeworkStatus.name
            )
            customHomeworkStatusDao.insert(customHomeworkStatus)
        }
    }

    private suspend fun importCustomHomeworkTypes(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<List<PythonCustomHomeworkType>>(content)
        pythonData.forEach { pythonCustomHomeworkType ->
            val customHomeworkType = CustomHomeworkType(
                name = pythonCustomHomeworkType.name
            )
            customHomeworkTypeDao.insert(customHomeworkType)
        }

    }
}