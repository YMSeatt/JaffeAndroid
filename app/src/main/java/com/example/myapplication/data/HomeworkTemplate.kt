package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

enum class HomeworkMarkType {
    CHECKBOX,
    SCORE,
    COMMENT
}

data class HomeworkMarkStep(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val type: HomeworkMarkType,
    val maxValue: Int = 1
)

@Entity(tableName = "homework_templates")
data class HomeworkTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val marksData: String // JSON string for List<HomeworkMarkStep>
) {
    fun getSteps(): List<HomeworkMarkStep> {
        return try {
            val type = object : TypeToken<List<HomeworkMarkStep>>() {}.type
            Gson().fromJson(marksData, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun fromSteps(name: String, steps: List<HomeworkMarkStep>, id: Long = 0): HomeworkTemplate {
            return HomeworkTemplate(
                id = id,
                name = name,
                marksData = Gson().toJson(steps)
            )
        }
    }
}

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
