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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JsonImporter: A legacy bridge for fragmented classroom data ingestion.
 *
 * This class facilitates the import of classroom data from individual, specialized JSON files.
 * It is primarily used to support older Python desktop application exports or scenarios where
 * data is provided as a collection of discrete files (e.g., behaviors, groups, and students
 * in separate files) rather than a unified v10 backup.
 *
 * ### Architectural Roles:
 * 1. **Fragmented Data Bridge**: Unlike the unified [Importer], this class coordinates the
 *    sequential ingestion of multiple URIs, ensuring that foundational data (like student groups)
 *    is imported before dependent data (like students).
 * 2. **String-ID Resolution**: Relies on [StudentDao.getStudentByStringId] to link logs to students,
 *    facilitating the ingestion of historical logs where local auto-increment IDs are unknown.
 * 3. **Template Migration**: Handles the conversion of Python-style homework templates into
 *    Android-compatible [HomeworkMarkType] structures.
 *
 * @param context Application context.
 * @param studentDao DAO for student operations.
 * @param furnitureDao DAO for furniture operations.
 * @param behaviorEventDao DAO for behavior events.
 * @param homeworkLogDao DAO for homework logs.
 * @param studentGroupDao DAO for student groups.
 */
@Singleton
class JsonImporter @Inject constructor(
    @ApplicationContext private val context: Context,
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

    /**
     * Executes a coordinated bulk import from multiple source URIs.
     *
     * The ingestion order is critical to maintaining relational integrity:
     * 1. **Student Groups**: Must exist so students can be assigned to them.
     * 2. **Classroom Data**: Ingests students and furniture.
     * 3. **Custom Categories**: Populates dropdown menus for behaviors and homework.
     * 4. **Templates**: (Optional) Ingests reusable assignment structures.
     */
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

    /**
     * Reads the content of a file from a [Uri] with a strict size limit.
     *
     * **Security Hardening**: Enforces a 50MB limit to prevent Out-of-Memory (OOM)
     * Denial-of-Service attacks when ingesting large, malicious, or malformed JSON files.
     */
    private fun readFileContent(uri: Uri): String {
        val maxSizeBytes = 50 * 1024 * 1024L // 50MB limit

        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
            if (afd.length > maxSizeBytes) {
                throw SecurityException("Import failed: File exceeds 50MB limit.")
            }
        }

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw java.io.FileNotFoundException("Could not open input stream for $uri")

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