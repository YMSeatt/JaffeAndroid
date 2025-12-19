package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(tableName = "homework_templates")
data class HomeworkTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val marksData: String // JSON string for marks data
)

@Entity(
    tableName = "homework",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HomeworkTemplate::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "student_id", index = true) val studentId: Long,
    @ColumnInfo(name = "template_id", index = true) val templateId: Long?,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)
