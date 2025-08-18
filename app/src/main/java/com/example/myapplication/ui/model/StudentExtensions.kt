package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
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
        displayBackgroundColor = groupColor?.let { Color(it.toColorInt()) } ?: this.customBackgroundColor?.let { Color(it.toColorInt()) } ?: Color.LightGray,
        displayOutlineColor = this.customOutlineColor?.let { Color(it.toColorInt()) } ?: Color.DarkGray,
        displayTextColor = this.customTextColor?.let { Color(it.toColorInt()) } ?: Color.Black,
        displayOutlineThickness = 1.dp,
        recentBehaviorDescription = recentBehaviorDescription
    )
}
