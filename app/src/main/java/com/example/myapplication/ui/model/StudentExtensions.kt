package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Student

fun Student.toStudentUiItem(
    recentBehaviorDescription: String?,
    groupColor: String?
): StudentUiItem {
    return StudentUiItem(
        id = this.id.toInt(),
        fullName = "${this.firstName} ${this.lastName}",
        initials = this.initials ?: this.getGeneratedInitials(),
        xPosition = this.xPosition.toDouble(),
        yPosition = this.yPosition.toDouble(),
        displayWidth = (this.customWidth ?: 120).dp,
        displayHeight = (this.customHeight ?: 100).dp,
        displayBackgroundColor = groupColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: this.customBackgroundColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.LightGray,
        displayOutlineColor = this.customOutlineColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.DarkGray,
        displayTextColor = this.customTextColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Black,
        displayOutlineThickness = 1.dp,
        recentBehaviorDescription = recentBehaviorDescription
    )
}
