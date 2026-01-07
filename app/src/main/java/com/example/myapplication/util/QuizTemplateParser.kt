package com.example.myapplication.util

object QuizTemplateParser {
    fun parseDefaultMarks(marksString: String): Map<String, Int> {
        if (marksString.isBlank()) {
            return emptyMap()
        }
        val marks = mutableMapOf<String, Int>()
        val pairs = marksString.split(',').map { it.trim() }
        for (pair in pairs) {
            val keyValue = pair.split(':').map { it.trim() }
            if (keyValue.size != 2) {
                throw IllegalArgumentException("Invalid format for pair: $pair")
            }
            val key = keyValue[0]
            val value = keyValue[1].toIntOrNull()
                ?: throw IllegalArgumentException("Value is not a number for pair: $pair")
            if (key.isBlank()) {
                throw IllegalArgumentException("Key cannot be blank for pair: $pair")
            }
            marks[key] = value
        }
        return marks
    }
}
