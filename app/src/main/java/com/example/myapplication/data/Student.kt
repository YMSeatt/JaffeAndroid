package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a student in the seating chart.
 * Contains basic information, positioning on the chart, and highly customizable UI styling.
 */
@Serializable
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Unique string identifier, often used for synchronization with external data (e.g., "student_123"). */
    val stringId: String? = null,
    val firstName: String,
    val lastName: String,
    var nickname: String? = null,
    /** Gender of the student, defaults to "Boy". Used for filtering and icons. */
    var gender: String = "Boy",
    /** Reference to the [StudentGroup] this student belongs to. */
    var groupId: Long? = null,
    /** Custom initials to display on the seating chart icon. If null, initials are generated from the name. */
    var initials: String? = null,
    /** Horizontal position on the seating chart canvas. */
    var xPosition: Float = 0f,
    /** Vertical position on the seating chart canvas. */
    var yPosition: Float = 0f,
    // --- UI Customization Fields ---
    // These fields allow for per-student styling overrides on the seating chart.
    // If null, the application-wide default styles (defined in UserPreferences) are used.

    /** Custom width for the student icon on the seating chart. */
    var customWidth: Int? = null,
    /** Custom height for the student icon on the seating chart. */
    var customHeight: Int? = null,
    /** Custom background color (Hex string). */
    var customBackgroundColor: String? = null,
    /** Custom border/outline color (Hex string). */
    var customOutlineColor: String? = null,
    /** Custom text color for the name (Hex string). */
    var customTextColor: String? = null,
    /** Custom thickness for the icon border. */
    var customOutlineThickness: Int? = null,
    /** Custom corner radius for the icon box. */
    var customCornerRadius: Int? = null,
    /** Custom internal padding for the icon box. */
    var customPadding: Int? = null,
    /** Custom font family name (e.g., "sans-serif", "monospace"). */
    var customFontFamily: String? = null,
    /** Custom font size in SP. */
    var customFontSize: Int? = null,
    /** Custom color for all text elements (Hex string). */
    var customFontColor: String? = null,
    /** A temporary instruction or task assigned to the student, displayed on their icon. */
    var temporaryTask: String? = null,
    /** Whether to show recent behavior/homework logs on the student's chart icon. */
    var showLogs: Boolean = true
) {
    // Secondary constructor or init block could be used for auto-generation
    // if a more complex logic is needed, or handle it in the ViewModel/Repository
    // For simplicity, we'll handle auto-generation when a Student object is created.
    /**
     * Generates initials from the first and last name (e.g., "John Doe" -> "JD").
     */
    fun getGeneratedInitials(): String {
        val firstInitial = firstName.firstOrNull()?.uppercaseChar() ?: ' '
        val lastInitial = lastName.firstOrNull()?.uppercaseChar() ?: ' '
        return "$firstInitial$lastInitial".trim()
    }
}
