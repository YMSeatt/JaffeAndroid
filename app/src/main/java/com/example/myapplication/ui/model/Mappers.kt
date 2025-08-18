package com.example.myapplication.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.Student

fun Student.toStudentUiItem(recentBehaviorDescription: String?, groupColor: String?): StudentUiItem {
    return StudentUiItem(
        id = this.id.toInt(),
        fullName = "${this.firstName} ${this.lastName}",
        initials = this.initials ?: "",
        xPosition = this.xPosition.toDouble(),
        yPosition = this.yPosition.toDouble(),
        displayWidth = (this.customWidth ?: 100).dp,
        displayHeight = (this.customHeight ?: 100).dp,
        displayBackgroundColor = this.customBackgroundColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Gray,
        displayOutlineColor = this.customOutlineColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Black,
        displayTextColor = this.customTextColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.White,
        displayOutlineThickness = 1.dp, // Assuming a default value
        recentBehaviorDescription = recentBehaviorDescription,
        groupColor = groupColor?.let { Color(android.graphics.Color.parseColor(it)) }
    )
}

fun Furniture.toUiItem(): FurnitureUiItem {
    return FurnitureUiItem(
        id = this.id,
        stringId = this.stringId,
        name = this.name,
        type = this.type,
        xPosition = this.xPosition,
        yPosition = this.yPosition,
        displayWidth = this.width.dp,
        displayHeight = this.height.dp,
        displayBackgroundColor = this.fillColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.LightGray,
        displayOutlineColor = this.outlineColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Black,
        displayTextColor = Color.Black, // Assuming a default value
        displayOutlineThickness = 1.dp // Assuming a default value
    )
}
