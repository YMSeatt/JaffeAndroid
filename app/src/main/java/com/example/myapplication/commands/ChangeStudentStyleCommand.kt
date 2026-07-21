package com.example.myapplication.commands

import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

/**
 * Command to update visual style overrides (e.g., color, font size) for a specific student.
 * Bridges the dynamic dictionary styling model of Python to Android's strongly typed, schema-based model.
 *
 * @param viewModel The ViewModel to perform the database operations.
 * @param studentId The ID of the student being styled.
 * @param styleProperty The style property to update (e.g., "customBackgroundColor").
 * @param oldValue The previous value of the style property.
 * @param newValue The new value to apply.
 */
class ChangeStudentStyleCommand(
    private val viewModel: SeatingChartViewModel,
    private val studentId: Long,
    private val styleProperty: String,
    private val oldValue: Any?,
    private val newValue: Any?
) : Command {

    override suspend fun execute() {
        val student = viewModel.getStudentForEditing(studentId) ?: return
        val updatedStudent = applyStyleProperty(student, styleProperty, newValue)
        viewModel.internalUpdateStudent(updatedStudent)
    }

    override suspend fun undo() {
        val student = viewModel.getStudentForEditing(studentId) ?: return
        val updatedStudent = applyStyleProperty(student, styleProperty, oldValue)
        viewModel.internalUpdateStudent(updatedStudent)
    }

    override fun getDescription(): String {
        return "Style Change: $styleProperty for student ID $studentId"
    }

    private fun applyStyleProperty(student: Student, property: String, value: Any?): Student {
        return when (property) {
            "backgroundColor", "customBackgroundColor" -> student.copy(customBackgroundColor = value as? String)
            "outlineColor", "customOutlineColor" -> student.copy(customOutlineColor = value as? String)
            "textColor", "customTextColor" -> student.copy(customTextColor = value as? String)
            "outlineThickness", "customOutlineThickness" -> student.copy(customOutlineThickness = value as? Int)
            "cornerRadius", "customCornerRadius" -> student.copy(customCornerRadius = value as? Int)
            "padding", "customPadding" -> student.copy(customPadding = value as? Int)
            "fontFamily", "customFontFamily" -> student.copy(customFontFamily = value as? String)
            "fontSize", "customFontSize" -> student.copy(customFontSize = value as? Int)
            "fontColor", "customFontColor" -> student.copy(customFontColor = value as? String)
            "width", "customWidth" -> student.copy(customWidth = value as? Int)
            "height", "customHeight" -> student.copy(customHeight = value as? Int)
            else -> throw IllegalArgumentException("Unknown style property: $property")
        }
    }
}
