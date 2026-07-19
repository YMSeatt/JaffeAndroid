package com.example.myapplication.data.importer

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.myapplication.data.AppDatabase
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
    private val securityUtil: com.example.myapplication.util.SecurityUtil,
    private val db: AppDatabase,
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
     * ### Order of Execution Heuristics:
     * Ingestion order is strictly structured to preserve relational/foreign-key integrity in SQLite:
     * 1. **Student Groups**: Must be imported first, enabling child Student records to link to valid group primary keys.
     * 2. **Classroom Data**: Ingests students and furniture items.
     * 3. **Custom Categories**: Populates dropdown menus for behaviors, homework types, and homework statuses.
     * 4. **Templates**: (Optional) Ingests reusable template structures for assignments.
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

    /**
     * Ingests homework template definitions from a JSON file.
     *
     * ### Paradigm & Structural Parity:
     * While Python templates are simpler (containing a flat checklist of items), Android homework templates
     * are relational and represent a multi-step workflow. This method normalizes Python's checklist models
     * into native [HomeworkMarkStep] elements with mapped [HomeworkMarkType]s:
     * - "checkbutton" / "checkbox" mapped to [HomeworkMarkType.CHECKBOX]
     * - "scale" / "slider" / "score" mapped to [HomeworkMarkType.SCORE]
     * - "entry" / "text" / "comment" mapped to [HomeworkMarkType.COMMENT]
     *
     * This translation guarantees that teachers maintain their custom grading criteria (e.g. effort, completeness)
     * when synchronizing files from the desktop workspace.
     */
    private suspend fun importHomeworkTemplates(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<List<PythonHomeworkTemplate>>(content)
        val templatesToInsert = pythonData.map { pythonTemplate ->
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
            HomeworkTemplate(
                name = pythonTemplate.name,
                marksData = json.encodeToString(steps)
            )
        }
        homeworkTemplateDao.insertAll(templatesToInsert)
    }

    /**
     * Reads raw text data from an Android content URI, enforcing security limits.
     *
     * ### Shield Hardening & DoS Prevention:
     * Parsing massive nested JSON arrays can trigger rapid memory escalation and Out-of-Memory (OOM) failures.
     * To protect the application's memory boundary, this method strictly wraps file streaming with a 50MB
     * security limit, aborting immediately if a file exceeds this cap.
     *
     * @throws SecurityException If the file exceeds the 50MB safety limit.
     */
    private fun readFileContent(uri: Uri): String {
        val maxSizeBytes = 50 * 1024 * 1024L // 50MB limit

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw java.io.FileNotFoundException("Could not open input stream for $uri")

        inputStream.use { stream ->
            val bos = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (stream.read(buffer).also { bytesRead = it } != -1) {
                totalBytesRead += bytesRead
                if (totalBytesRead > maxSizeBytes) {
                    throw SecurityException("Import failed: File exceeds 50MB limit.")
                }
                bos.write(buffer, 0, bytesRead)
            }
            return bos.toString("UTF-8")
        }
    }

    /**
     * Processes individual student, furniture, and timeline logs from a classroom export.
     *
     * ### Multi-Pass Transaction & Referential Integrity:
     * Room requires rigid relational mappings. If logs were inserted before students, foreign key constraints
     * would fail, resulting in database corruption or runtime crashes. To ensure absolute data safety, this
     * method coordinates a **4-Pass Ingestion pipeline** inside a single database transaction:
     * 1. **Pass 1: Students**: Student profiles are imported first. We pre-fetch [StudentGroup]s into an
     *    in-memory cache to resolve group IDs in $O(1)$ time, eliminating the N+1 query problem. After insertion,
     *    we build a mapping of Python string UUIDs to Android auto-incremented primary keys.
     * 2. **Pass 2: Furniture**: Physical classroom boundaries and desks are loaded.
     * 3. **Pass 3: Behavior Logs**: Historical events are loaded, linking student references back to their Android IDs.
     * 4. **Pass 4: Homework Logs**: Longitudinal homework completion records are populated, converting Python-style
     *    detail maps into encrypted JSON strings.
     *
     * All operations use Room transactions (`db.withTransaction`) to ensure that either the entire import succeeds
     * or the database is rolled back, preventing orphaned or half-imported database records.
     */
    private suspend fun importClassroomData(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<PythonClassroomData>(content)

        db.withTransaction {
            // BOLT: Pre-fetch all student groups into a map for O(1) lookup during student import.
            val studentGroups = studentGroupDao.getAllStudentGroupsList().associateBy { it.name }

            // Pass 1: Import Students
            val studentEntries = pythonData.students.entries.toList()
            val studentsToInsert = studentEntries.map { (stringId, pythonStudent) ->
                Student(
                    stringId = stringId,
                    firstName = securityUtil.encrypt(pythonStudent.firstName),
                    lastName = securityUtil.encrypt(pythonStudent.lastName),
                    nickname = if (pythonStudent.nickname.isBlank()) null else securityUtil.encrypt(pythonStudent.nickname),
                    gender = pythonStudent.gender,
                    xPosition = pythonStudent.x.toFloat(),
                    yPosition = pythonStudent.y.toFloat(),
                    customWidth = pythonStudent.styleOverrides.width?.toInt(),
                    customHeight = pythonStudent.styleOverrides.height?.toInt(),
                    customBackgroundColor = pythonStudent.styleOverrides.fillColor,
                    customOutlineColor = pythonStudent.styleOverrides.outlineColor,
                    customTextColor = pythonStudent.styleOverrides.textColor,
                    groupId = studentGroups[pythonStudent.groupId]?.id,
                    temporaryTask = pythonStudent.temporaryTask?.let { securityUtil.encrypt(it) }
                )
            }
            val insertedStudentIds = studentDao.insertAll(studentsToInsert)
            // Create a mapping from Python-style ID (from input Map key) to local Long ID for O(1) resolution.
            val studentIdMap = studentEntries.indices.associate { i -> studentEntries[i].key to insertedStudentIds[i] }

            // Pass 2: Import Furniture
            val furnitureToInsert = pythonData.furniture.map { (stringId, pythonFurniture) ->
                Furniture(
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
            }
            furnitureDao.insertAll(furnitureToInsert)

            // Pass 3: Import Behavior Logs
            val behaviorEventsToInsert = pythonData.behaviorLog.mapNotNull { pythonBehaviorLog ->
                studentIdMap[pythonBehaviorLog.studentId]?.let { localId ->
                    BehaviorEvent(
                        studentId = localId,
                        type = pythonBehaviorLog.behavior,
                        timestamp = LocalDateTime.parse(pythonBehaviorLog.timestamp)
                            .toEpochSecond(ZoneOffset.UTC),
                        comment = pythonBehaviorLog.comment?.let { securityUtil.encrypt(it) }
                    )
                }
            }
            behaviorEventDao.insertAll(behaviorEventsToInsert)

            // Pass 4: Import Homework Logs
            val homeworkLogsToInsert = pythonData.homeworkLog.mapNotNull { pythonHomeworkLog ->
                studentIdMap[pythonHomeworkLog.studentId]?.let { localId ->
                    HomeworkLog(
                        studentId = localId,
                        assignmentName = pythonHomeworkLog.homeworkType,
                        status = pythonHomeworkLog.behavior,
                        loggedAt = LocalDateTime.parse(pythonHomeworkLog.timestamp)
                            .toEpochSecond(ZoneOffset.UTC),
                        comment = pythonHomeworkLog.comment?.let { securityUtil.encrypt(it) },
                        marksData = pythonHomeworkLog.homeworkDetails?.let { securityUtil.encrypt(json.encodeToString(it)) }
                    )
                }
            }
            homeworkLogDao.insertAll(homeworkLogsToInsert)
        }
    }

    /**
     * Imports custom student groups and registers them.
     *
     * Maps Python group configurations (name and visual hex color mapping) into native [StudentGroup] entities.
     */
    private suspend fun importStudentGroups(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<Map<String, PythonStudentGroup>>(content)
        val groupsToInsert = pythonData.values.map { pythonStudentGroup ->
            StudentGroup(
                name = pythonStudentGroup.name,
                color = pythonStudentGroup.color
            )
        }
        studentGroupDao.insertAll(groupsToInsert)
    }

    /**
     * Imports user-defined custom behavior categories to populate the quick-logging selection menus.
     */
    private suspend fun importCustomBehaviors(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<List<PythonCustomBehavior>>(content)
        val behaviorsToInsert = pythonData.map { pythonCustomBehavior ->
            CustomBehavior(
                name = pythonCustomBehavior.name
            )
        }
        customBehaviorDao.insertAll(behaviorsToInsert)
    }

    /**
     * Imports custom homework completion statuses (such as "Incomplete", "Excellent", or "Redo") to ensure
     * customized classroom schemas are faithfully preserved.
     */
    private suspend fun importCustomHomeworkStatuses(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<List<PythonCustomHomeworkStatus>>(content)
        val statusesToInsert = pythonData.map { pythonCustomHomeworkStatus ->
            CustomHomeworkStatus(
                name = pythonCustomHomeworkStatus.name
            )
        }
        customHomeworkStatusDao.insertAll(statusesToInsert)
    }

    /**
     * Ingests custom homework assignment categories (e.g. "Worksheets", "Labs", "Projects") to maintain
     * assignment filtering parity across platforms.
     */
    private suspend fun importCustomHomeworkTypes(uri: Uri) {
        val content = readFileContent(uri)
        val pythonData = json.decodeFromString<List<PythonCustomHomeworkType>>(content)
        val typesToInsert = pythonData.map { pythonCustomHomeworkType ->
            CustomHomeworkType(
                name = pythonCustomHomeworkType.name
            )
        }
        customHomeworkTypeDao.insertAll(typesToInsert)
    }
}