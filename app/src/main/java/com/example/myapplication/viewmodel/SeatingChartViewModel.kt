package com.example.myapplication.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.LayoutTemplate
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.data.StudentGroupDao
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.ui.model.FurnitureUiItem
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.model.toStudentUiItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Stack

data class SeatingChartState(
    val students: List<Student>,
    val furniture: List<Furniture>
)

class SeatingChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudentRepository
    private val studentDao: StudentDao
    private val furnitureDao = AppDatabase.getDatabase(application).furnitureDao()
    private val layoutTemplateDao = AppDatabase.getDatabase(application).layoutTemplateDao()
    private val behaviorEventDao = AppDatabase.getDatabase(application).behaviorEventDao()
    private val quizLogDao = AppDatabase.getDatabase(application).quizLogDao()
    private val homeworkLogDao = AppDatabase.getDatabase(application).homeworkLogDao()
    private val studentGroupDao: StudentGroupDao = AppDatabase.getDatabase(application).studentGroupDao()


    val allStudents: LiveData<List<Student>>
    val allFurniture: LiveData<List<Furniture>>
    val allLayoutTemplates: LiveData<List<LayoutTemplate>>
    val studentsForDisplay = MediatorLiveData<List<StudentUiItem>>()
    val furnitureForDisplay = MediatorLiveData<List<FurnitureUiItem>>()

    private val undoStack = Stack<SeatingChartState>()
    private val redoStack = Stack<SeatingChartState>()

    // In-memory session data
    private var sessionQuizLogs = mutableListOf<QuizLog>()
    private var sessionHomeworkLogs = mutableListOf<HomeworkLog>()
    private var isSessionActive = false

    init {
        val studentDb = AppDatabase.getDatabase(application)
        studentDao = studentDb.studentDao()
        repository = StudentRepository(studentDao, studentGroupDao)
        allStudents = repository.allStudents
        allFurniture = furnitureDao.getAllFurniture()
        allLayoutTemplates = layoutTemplateDao.getAllTemplates()

        studentsForDisplay.addSource(allStudents) { students ->
            updateStudentsForDisplay()
        }
        studentsForDisplay.addSource(studentGroupDao.getAllStudentGroups()) { groups ->
            updateStudentsForDisplay()
        }

        furnitureForDisplay.addSource(allFurniture) { furnitureList ->
            furnitureForDisplay.value = furnitureList.map { it.toUiItem() }
        }
    }

    private fun updateStudentsForDisplay() {
        viewModelScope.launch {
            val students = allStudents.value ?: return@launch
            val groups = studentGroupDao.getAllStudentGroups().first() // Use first() to get current value
            val studentsWithBehavior = students.map { student ->
                val mostRecentEvent = behaviorEventDao.getMostRecentBehaviorForStudent(student.id)
                student.toStudentUiItem(
                    recentBehaviorDescription = mostRecentEvent?.type,
                    groupColor = groups.find { it.id == student.groupId }?.color
                )
            }
            studentsForDisplay.postValue(studentsWithBehavior)
        }
    }


    private fun saveStateForUndo() {
        val currentState = SeatingChartState(
            students = allStudents.value ?: emptyList(),
            furniture = allFurniture.value ?: emptyList()
        )
        undoStack.push(currentState)
        redoStack.clear() // Clear redo stack whenever a new action is performed
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentState = SeatingChartState(
                students = allStudents.value ?: emptyList(),
                furniture = allFurniture.value ?: emptyList()
            )
            redoStack.push(currentState)

            val prevState = undoStack.pop()
            viewModelScope.launch(Dispatchers.IO) {
                studentDao.updateAll(prevState.students)
                furnitureDao.updateAll(prevState.furniture)
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentState = SeatingChartState(
                students = allStudents.value ?: emptyList(),
                furniture = allFurniture.value ?: emptyList()
            )
            undoStack.push(currentState)

            val nextState = redoStack.pop()
            viewModelScope.launch(Dispatchers.IO) {
                studentDao.updateAll(nextState.students)
                furnitureDao.updateAll(nextState.furniture)
            }
        }
    }

    fun getAllStudentsForExport(): List<Student>? {
        return allStudents.value
    }


    // Student operations
    fun addStudent(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        saveStateForUndo()
        studentDao.insert(student)
    }

    fun updateStudent(oldStudent: Student, newStudent: Student) = viewModelScope.launch(Dispatchers.IO) {
        saveStateForUndo()
        val originalState = SeatingChartState(
            students = allStudents.value?.map { if (it.id == oldStudent.id) oldStudent else it } ?: emptyList(),
            furniture = allFurniture.value ?: emptyList()
        )
        // This is a simplified undo state. A more robust implementation might need more details.
        // For now, we're just saving the state before the update.
        undoStack.push(originalState)

        studentDao.update(newStudent)
    }


    fun deleteStudent(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        saveStateForUndo()
        studentDao.delete(student)
    }

    fun updateStudentPosition(studentId: Int, oldX: Float, oldY: Float, newX: Float, newY: Float) = viewModelScope.launch(Dispatchers.IO) {
        // Find the original student state before the drag
        val originalStudent = studentDao.getStudentById(studentId)
        originalStudent?.let {
            val originalState = SeatingChartState(
                students = allStudents.value?.map {
                    if (it.id == studentId) it.copy(xPosition = oldX, yPosition = oldY) else it
                } ?: emptyList(),
                furniture = allFurniture.value ?: emptyList()
            )
            undoStack.push(originalState)
        }

        studentDao.updatePosition(studentId, newX, newY)
    }

    suspend fun getStudentForEditing(studentId: Int): Student? = withContext(Dispatchers.IO) {
        return@withContext studentDao.getStudentById(studentId)
    }

    // Furniture operations
    fun addFurniture(furniture: Furniture) = viewModelScope.launch(Dispatchers.IO) {
        saveStateForUndo()
        furnitureDao.insert(furniture)
    }

    fun updateFurniture(furniture: Furniture) = viewModelScope.launch(Dispatchers.IO) {
        saveStateForUndo()
        furnitureDao.update(furniture)
    }

    fun deleteFurnitureById(furnitureId: Int) = viewModelScope.launch(Dispatchers.IO) {
        saveStateForUndo()
        furnitureDao.deleteById(furnitureId)
    }

// In SeatingChartViewModel.kt

    fun updateFurniturePosition(furnitureId: Int, newX: Float, newY: Float) = viewModelScope.launch(Dispatchers.IO) {
        val originalFurniture = furnitureDao.getFurnitureById(furnitureId)
        originalFurniture?.let {
            val originalState = SeatingChartState(
                students = allStudents.value ?: emptyList(),
                furniture = allFurniture.value?.map {
                    if (it.id == furnitureId) it.copy(xPosition = originalFurniture.xPosition, yPosition = originalFurniture.yPosition) else it
                } ?: emptyList()
            )
            undoStack.push(originalState)
        }
        furnitureDao.updatePosition(furnitureId, newX, newY)
    }


    suspend fun getFurnitureById(furnitureId: Int): Furniture? = withContext(Dispatchers.IO) {
        return@withContext furnitureDao.getFurnitureById(furnitureId)
    }

    // Layout operations
    fun saveLayout(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val studentsJson = JSONArray(allStudents.value?.map { student ->
            JSONObject().apply {
                put("id", student.id)
                put("x", student.xPosition)
                put("y", student.yPosition)
            }
        }).toString()

        val furnitureJson = JSONArray(allFurniture.value?.map { furniture ->
            JSONObject().apply {
                put("id", furniture.id)
                put("x", furniture.xPosition)
                put("y", furniture.yPosition)
            }
        }).toString()

        val layout = LayoutTemplate(name = name, studentPositions = studentsJson, furniturePositions = furnitureJson)
        layoutTemplateDao.insert(layout)
    }

    fun loadLayout(layout: LayoutTemplate) = viewModelScope.launch(Dispatchers.IO) {
        saveStateForUndo()
        val studentPositions = JSONArray(layout.studentPositions)
        for (i in 0 until studentPositions.length()) {
            val pos = studentPositions.getJSONObject(i)
            studentDao.updatePosition(pos.getInt("id"), pos.getDouble("x").toFloat(), pos.getDouble("y").toFloat())
        }

        val furniturePositions = JSONArray(layout.furniturePositions)
        for (i in 0 until furniturePositions.length()) {
            val pos = furniturePositions.getJSONObject(i)
            furnitureDao.updatePosition(pos.getInt("id"), pos.getDouble("x").toFloat(), pos.getDouble("y").toFloat())
        }
    }

    fun deleteLayoutTemplate(layout: LayoutTemplate) = viewModelScope.launch(Dispatchers.IO) {
        layoutTemplateDao.delete(layout)
    }

    fun addBehaviorEvent(event: BehaviorEvent) = viewModelScope.launch(Dispatchers.IO) {
        behaviorEventDao.insert(event)
        withContext(Dispatchers.Main) {
            updateStudentsForDisplay()
        }
    }
    // QuizLog operations
    fun saveQuizLog(log: QuizLog) = viewModelScope.launch(Dispatchers.IO) {
        quizLogDao.insert(log)
    }

    // HomeworkLog operations
    fun addHomeworkLog(log: HomeworkLog) = viewModelScope.launch(Dispatchers.IO) {
        homeworkLogDao.insert(log)
    }

    fun startSession() {
        isSessionActive = true
        sessionQuizLogs.clear()
        sessionHomeworkLogs.clear()
        Log.d("SeatingChartViewModel", "Session started.")
    }

    fun endSession() {
        if (isSessionActive) {
            viewModelScope.launch(Dispatchers.IO) {
                // Batch insert the session logs into the database
                quizLogDao.insertAll(sessionQuizLogs)
                homeworkLogDao.insertAll(sessionHomeworkLogs)
                Log.d("SeatingChartViewModel", "Session ended. Saved ${sessionQuizLogs.size} quiz logs and ${sessionHomeworkLogs.size} homework logs.")
                // Clear the session data
                sessionQuizLogs.clear()
                sessionHomeworkLogs.clear()
                isSessionActive = false
            }
        }
    }


    fun addQuizLogToSession(quizLog: QuizLog) {
        if (isSessionActive) {
            sessionQuizLogs.add(quizLog)
            Log.d("SeatingChartViewModel", "Quiz log added to session for student ${quizLog.studentId}.")
        } else {
            // If not in a session, save directly to the database
            saveQuizLog(quizLog)
        }
    }

// In SeatingChartViewModel.kt, add a similar method for homework logs

    fun addHomeworkLogToSession(homeworkLog: HomeworkLog) {
        if (isSessionActive) {
            sessionHomeworkLogs.add(homeworkLog)
            Log.d("SeatingChartViewModel", "Homework log added to session for student ${homeworkLog.studentId}.")
        } else {
            addHomeworkLog(homeworkLog)
        }
    }

    private val _groupsForRandomAssignment = MutableLiveData<List<StudentGroup>>()
    val groupsForRandomAssignment: LiveData<List<StudentGroup>> = _groupsForRandomAssignment

    fun loadGroupsForRandomAssignment() {
        viewModelScope.launch {
            _groupsForRandomAssignment.value = studentGroupDao.getAllStudentGroups().first()
        }
    }

    fun performRandomAssignment(
        selectedGroupIds: List<Long>,
        assignmentType: String, // "PAIRS" or "THREES"
        shouldIncludeUngrouped: Boolean
    ) {
        viewModelScope.launch {
            saveStateForUndo()

            // 1. Fetch students based on selection
            val studentsToAssign = mutableListOf<Student>()
            val selectedStudents = repository.getStudentsByGroupIds(selectedGroupIds)
            studentsToAssign.addAll(selectedStudents)

            if (shouldIncludeUngrouped) {
                val ungroupedStudents = repository.getUngroupedStudents()
                studentsToAssign.addAll(ungroupedStudents)
            }

            // 2. Shuffle and create new groups
            studentsToAssign.shuffle()
            val newSubgroups = mutableListOf<List<Student>>()
            val groupSize = if (assignmentType == "PAIRS") 2 else 3

            for (i in studentsToAssign.indices step groupSize) {
                val end = minOf(i + groupSize, studentsToAssign.size)
                newSubgroups.add(studentsToAssign.subList(i, end))
            }
            if (newSubgroups.size > 1 && newSubgroups.last().size == 1) {
                val lastStudent = newSubgroups.removeLast().first()
                newSubgroups.last().toMutableList().add(lastStudent)
            }


            // 3. Update student positions (simplified logic)
            // A more complex implementation would arrange them nicely on the screen.
            var currentX = 50f
            val startY = 50f
            val spacingX = 120f
            val spacingY = 120f
            val rowWidth = 600f // Approximate width of the screen area for seating

            newSubgroups.forEach { subgroup ->
                var currentY = startY
                subgroup.forEach { student ->
                    student.xPosition = currentX
                    student.yPosition = currentY
                    currentY += spacingY
                }
                currentX += spacingX
                if (currentX > rowWidth) {
                    currentX = 50f
                    // You might want to increment a row Y-position here for a grid layout
                }
            }
            studentDao.updateAll(studentsToAssign)
        }
    }
}

private fun Furniture.toUiItem(): FurnitureUiItem {
    return FurnitureUiItem(
        id = this.id,
        name = this.name,
        xPosition = this.xPosition,
        yPosition = this.yPosition,
        // Example of how you might determine display properties.
        // In a real app, this could be based on furniture.type or other properties.
        displayWidth = this.width.dp,
        displayHeight = this.height.dp
        // Other properties determined here
    )
}
