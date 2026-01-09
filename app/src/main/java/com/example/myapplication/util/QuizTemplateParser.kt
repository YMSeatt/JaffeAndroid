package com.example.myapplication.util

object QuizTemplateParser {
    fun parseDefaultMarks(input: String): Map<String, Int> {
        if (input.isBlank()) {
            return emptyMap()
        }
        return input.split(",")
            .mapNotNull { pair ->
                val parts = pair.trim().split(":")
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

    fun formatDefaultMarks(marks: Map<String, Int>): String {
        return marks.entries.joinToString(separator = ", ") { (key, value) ->
            "$key:$value"
        }
    }
}
