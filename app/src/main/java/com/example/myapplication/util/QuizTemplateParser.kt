package com.example.myapplication.util

object QuizTemplateParser {
    fun parseMarks(marksString: String): Map<String, Int> {
        if (marksString.isBlank()) return emptyMap()
        return marksString.split(",")
            .map { it.trim() }
            .filter { it.contains(":") }
            .associate {
                val (key, value) = it.split(":", limit = 2)
                key.trim() to (value.trim().toIntOrNull() ?: throw IllegalArgumentException("Invalid integer value for key $key"))
            }
    }

    fun marksToString(marks: Map<String, Int>?): String {
        return marks?.map { "${it.key}:${it.value}" }?.joinToString(", ") ?: ""
    }
}
