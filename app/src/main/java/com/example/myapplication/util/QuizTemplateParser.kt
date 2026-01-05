package com.example.myapplication.util

object QuizTemplateParser {
    fun parseDefaultMarks(marksString: String): Map<String, Int> {
        return marksString.split(',')
            .map { it.split(':') }
            .filter { it.size == 2 }
            .mapNotNull { parts ->
                val key = parts[0].trim()
                val value = parts[1].trim().toIntOrNull()
                if (key.isNotBlank() && value != null) {
                    key to value
                } else {
                    null
                }
            }
            .toMap()
    }
}
