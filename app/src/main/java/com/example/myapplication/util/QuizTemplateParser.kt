package com.example.myapplication.util

object QuizTemplateParser {

    fun parseMarks(marksString: String): Map<String, Int> {
        if (marksString.isBlank()) {
            return emptyMap()
        }
        return marksString.split(",")
            .mapNotNull { entry ->
                val parts = entry.trim().split(":")
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim().toIntOrNull()
                    if (key.isNotBlank() && value != null) {
                        key to value
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            .toMap()
    }

    fun formatMarks(marksMap: Map<String, Int>): String {
        return marksMap.map { "${it.key}: ${it.value}" }.joinToString(", ")
    }
}
