package com.example.myapplication.data

import androidx.room.Ignore

data class StudentDetailsForDisplay(
    // Fields directly mapped by Room from the query
    val id: Int,
    val firstName: String,
    val lastName: String,
    var initials: String?, // Added initials field
    val xPosition: Double,
    val yPosition: Double,
    val recentBehaviorDescription: String? = null,
    val customWidth: Int? = null,
    val customHeight: Int? = null,
    val customBackgroundColor: String? = null,
    val customOutlineColor: String? = null,
    val customTextColor: String? = null
) {
    // Fields calculated/set by the ViewModel AFTER Room has created the object
    // These are not part of the Room-mapped constructor.
    @Ignore
    var groupId: Long? = null
    @Ignore
    var displayWidth: Int = 120 // Default width from AppPreferencesRepository constants
    @Ignore
    var displayHeight: Int = 100 // Default height from AppPreferencesRepository constants
    @Ignore
    var displayBackgroundColor: String = "#FFCCCCCC" // Default BG (Light Gray opaque)
    @Ignore
    var displayOutlineColor: String = "#FF666666"   // Default Outline (Dark Gray opaque)
    @Ignore
    var displayTextColor: String = "#FF000000"      // Default Text (Black opaque)

    // Helper to get or generate initials
    fun getEffectiveInitials(): String {
        return if (!initials.isNullOrBlank()) {
            initials!!
        } else {
            val firstInitial = firstName.firstOrNull()?.uppercaseChar() ?: ' '
            val lastInitial = lastName.firstOrNull()?.uppercaseChar() ?: ' '
            "$firstInitial$lastInitial".trim()
        }
    }
}
