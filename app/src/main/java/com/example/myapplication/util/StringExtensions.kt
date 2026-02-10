package com.example.myapplication.util

import java.util.Locale

/**
 * Converts a string to Title Case, handling multiple words and common delimiters
 * like spaces, underscores, and hyphens.
 * e.g., "QUIZ_LOG" becomes "Quiz Log", "john-doe" becomes "John Doe".
 */
fun String.toTitleCase(): String {
    if (this.isBlank()) return this
    return this.split(Regex("[\\s_\\-]+"))
        .filter { it.isNotEmpty() }
        .joinToString(" ") { word ->
            word.lowercase(Locale.getDefault()).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
}

private val HEX_CHARS = "0123456789abcdef".toCharArray()

/**
 * Converts a byte array to a hexadecimal string representation efficiently.
 */
fun ByteArray.toHex(): String {
    val result = CharArray(size * 2)
    forEachIndexed { i, byte ->
        val v = byte.toInt() and 0xFF
        result[i * 2] = HEX_CHARS[v ushr 4]
        result[i * 2 + 1] = HEX_CHARS[v and 0x0F]
    }
    return String(result)
}

/**
 * Converts a hexadecimal string back to its byte array representation.
 */
fun String.hexToByteArray(): ByteArray {
    val result = ByteArray(length / 2)
    for (i in 0 until length step 2) {
        val firstDigit = Character.digit(this[i], 16)
        val secondDigit = Character.digit(this[i + 1], 16)
        result[i / 2] = ((firstDigit shl 4) + secondDigit).toByte()
    }
    return result
}
