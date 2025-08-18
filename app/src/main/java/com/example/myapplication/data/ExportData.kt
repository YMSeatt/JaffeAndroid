package com.example.myapplication.data

data class ExportData(
    val students: List<Student>,
    val behaviorEvents: List<BehaviorEvent>,
    val homeworkLogs: List<HomeworkLog>,
    val quizLogs: List<QuizLog>
)