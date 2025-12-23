package com.example.myapplication.util

import java.security.MessageDigest

object SecurityUtil {
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashedBytes = digest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPasswordSha256(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
