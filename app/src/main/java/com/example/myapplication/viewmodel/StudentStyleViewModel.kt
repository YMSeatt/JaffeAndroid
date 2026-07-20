package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Student
import com.example.myapplication.data.DefaultStudentStyle
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.preferences.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * StudentStyleViewModel: Manages custom visual styling and baseline appearances for student cards.
 *
 * This ViewModel bridges the user's aesthetic preferences with individual student visual states.
 * It coordinates two distinct styling paradigms:
 * 1. **Baseline Global Styles**: Defined in [DefaultStudentStyle] (from Jetpack DataStore preferences),
 *    which govern the universal appearance (size, background, outlines, fonts) of all student boxes.
 * 2. **Per-Student Overrides**: Encapsulated directly on the [Student] entity (e.g., specific outline colors, text colors,
 *    sizing) to highlight or categorize individual students dynamically on the interactive seating chart.
 *
 * ### Architectural Integration:
 * - **Unidirectional Data Flow (UDF)**: Exposes UI state reactively via read-only [StateFlow] streams.
 * - **History Management**: To preserve the integrity of the seating chart undo/redo history, the style update
 *   method delegates to the central [SeatingChartViewModel]. This ensures that changes to student visuals
 *   can be fully reversed.
 *
 * @property repository The source-of-truth student data repository for fetching custom styles.
 * @param preferencesRepository Repository for observing global aesthetic settings.
 */
@HiltViewModel
class StudentStyleViewModel @Inject constructor(
    private val repository: StudentRepository,
    preferencesRepository: AppPreferencesRepository
) : ViewModel() {

    /**
     * Internal backing state containing the currently focused [Student] record.
     */
    private val _student = MutableStateFlow<Student?>(null)

    /**
     * Read-only public [StateFlow] stream of the currently focused student.
     * Observed by the Style Editor dialog/screen to display current overrides.
     */
    val student: StateFlow<Student?> = _student.asStateFlow()

    /**
     * Reactive stream of the global [DefaultStudentStyle].
     * Uses [SharingStarted.WhileSubscribed] to conserve resources when the Style Editor is not active,
     * defaulting to standard application constants if not yet cached.
     */
    val defaultStudentStyle: StateFlow<DefaultStudentStyle> = preferencesRepository.defaultStudentStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultStudentStyle(
            backgroundColor = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_BG_COLOR_HEX,
            outlineColor = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX,
            textColor = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX,
            width = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_WIDTH_DP,
            height = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_HEIGHT_DP,
            outlineThickness = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP,
            fontFamily = com.example.myapplication.preferences.DEFAULT_STUDENT_FONT_FAMILY,
            fontSize = com.example.myapplication.preferences.DEFAULT_STUDENT_FONT_SIZE_SP,
            fontColor = com.example.myapplication.preferences.DEFAULT_STUDENT_FONT_COLOR_HEX,
            cornerRadius = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP,
            padding = com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_PADDING_DP
        ))

    /**
     * Asynchronously loads a student's profile from the database to initialize style editing.
     *
     * @param studentId The unique identifier of the target student.
     */
    fun loadStudent(studentId: Long) {
        viewModelScope.launch {
            _student.value = repository.getStudentById(studentId)
        }
    }

    /**
     * Persists customized visual updates for a student.
     *
     * ### Architectural Workflow:
     * 1. Updates are sent via the [seatingChartViewModel]'s `updateStudentStyle` method to create
     *    a reversible [com.example.myapplication.commands.Command] within the Undo history.
     * 2. After the asynchronous write finishes, the student profile is explicitly re-fetched from
     *    the local repository to ensure [student] state matches the newly persisted database values.
     *
     * @param seatingChartViewModel The central controller used to execute the style change command.
     * @param student The [Student] instance with updated style properties.
     */
    fun updateStudent(seatingChartViewModel: SeatingChartViewModel, student: Student) {
        viewModelScope.launch { // Launch a coroutine for the update and subsequent load
            seatingChartViewModel.updateStudentStyle(student)
            // After the update, explicitly reload the student to ensure the StateFlow is refreshed
            _student.value = repository.getStudentById(student.id)
        }
    }
}
