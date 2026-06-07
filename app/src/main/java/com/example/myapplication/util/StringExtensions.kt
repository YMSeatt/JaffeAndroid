package com.example.myapplication.util

import java.util.Locale

/**
 * Converts a string to Title Case, handling multiple words and common delimiters
 * like spaces, underscores, and hyphens.
 * e.g., "QUIZ_LOG" becomes "Quiz Log", "john-doe" becomes "John Doe".
 */
/**
 * BOLT: Optimized single-pass Title Case conversion.
 * Replaces Regex-based split/filter/join logic to eliminate intermediate list
 * and string allocations.
 */
fun String.toTitleCase(): String {
    if (this.isBlank()) return ""
    val locale = Locale.getDefault()
    val length = this.length
    val result = StringBuilder(length)
    var capitalizeNext = true
    var lastWasDelimiter = false

    for (i in 0 until length) {
        val c = this[i]
        if (c.isWhitespace() || c == '_' || c == '-') {
            if (!lastWasDelimiter && result.isNotEmpty()) {
                result.append(' ')
            }
            capitalizeNext = true
            lastWasDelimiter = true
        } else {
            if (capitalizeNext) {
                result.append(c.titlecase(locale))
                capitalizeNext = false
            } else {
                result.append(c.lowercase(locale))
            }
            lastWasDelimiter = false
        }
    }
    // Trim to handle leading/trailing delimiters
    return result.toString().trim()
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

/**
 * Masks an email address to protect PII in logs and UI components.
 * e.g., "john.doe@gmail.com" -> "j****@gmail.com"
 */
fun String.maskEmail(): String {
    val atIndex = indexOf('@')
    if (atIndex <= 1) return this
    val username = substring(0, atIndex)
    val domain = substring(atIndex)
    return username.take(1) + "****" + domain
}

/**
 * Masks a student's name to protect PII in shared artifacts and reports.
 *
 * Pattern:
 * - "John Doe" -> "J. DOE"
 * - "John" -> "J****"
 */
fun maskStudentName(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isBlank()) return ""

    val parts = trimmed.split(Regex("\\s+"))
    return if (parts.size >= 2) {
        val firstInitial = parts.first().take(1).uppercase(Locale.US)
        val lastName = parts.last().uppercase(Locale.US)
        "$firstInitial. $lastName"
    } else {
        val firstChar = trimmed.take(1).uppercase(Locale.US)
        if (trimmed.length > 1) {
            "$firstChar****"
        } else {
            firstChar
        }
    }
}

/**
 * Generates short initials for a multi-word string.
 * e.g., "Great Participation" -> "GP", "Off Task" -> "OT".
 *
 * This is a mobile-optimized port of the Python blueprint:
 * ''.join(part[0].upper() for part in name.split() if part)
 */
fun String.generateLogInitials(): String {
    if (this.isBlank()) return ""
    val result = StringBuilder()
    var isNewWord = true

    for (i in this.indices) {
        val c = this[i]
        if (c.isWhitespace()) {
            isNewWord = true
        } else {
            if (isNewWord) {
                result.append(c.uppercaseChar())
                isNewWord = false
            }
        }
    }
    return result.toString()
}
