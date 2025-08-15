package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stringId: String? = null, // For JSON sync, e.g., "student_1"
    val firstName: String,
    val lastName: String,
    var nickname: String? = null,
    var gender: String = "Boy",
    var groupId: String? = null,
    var initials: String? = null, // Customizable, can be null
    var xPosition: Float = 0f,
    var yPosition: Float = 0f,
    val customWidth: Int? = null,           // e.g., in dp
    val customHeight: Int? = null,          // e.g., in dp
    val customBackgroundColor: String? = null, // e.g., hex string like "#FFFFFF"
    val customOutlineColor: String? = null,  // e.g., hex string
    val customTextColor: String? = null      // e.g., hex string
) {
    // Secondary constructor or init block could be used for auto-generation
    // if a more complex logic is needed, or handle it in the ViewModel/Repository
    // For simplicity, we'll handle auto-generation when a Student object is created.
    fun getGeneratedInitials(): String {
        val firstInitial = firstName.firstOrNull()?.uppercaseChar() ?: ' '
        val lastInitial = lastName.firstOrNull()?.uppercaseChar() ?: ' '
        return "$firstInitial$lastInitial".trim()
    }
}
