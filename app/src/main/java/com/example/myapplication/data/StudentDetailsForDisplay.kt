package com.example.myapplication.data

import androidx.room.Ignore

data class StudentDetailsForDisplay(
    // Fields directly mapped by Room from the query
    var id: Long = 0,
    var firstName: String = "",
    var lastName: String = "",
    var initials: String? = null, // Added initials field
    var xPosition: Double = 0.0,
    var yPosition: Double = 0.0,
    var customWidth: Int? = null,
    var customHeight: Int? = null,
    var customBackgroundColor: String? = null,
    var customOutlineColor: String? = null,
    var customTextColor: String? = null
) {
    @Ignore
    var groupId: Long? = null
    @Ignore
    var recentBehaviorDescription: String? = null
    // Fields calculated/set by the ViewModel AFTER Room has created the object
    // These are not part of the Room-mapped constructor.
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
