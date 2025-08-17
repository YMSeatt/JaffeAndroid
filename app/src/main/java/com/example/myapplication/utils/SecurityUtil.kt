package com.example.myapplication.utils

object SecurityUtil {
    fun hashPassword(password: String): String {
        // In a real app, use a strong hashing algorithm like BCrypt or SCrypt.
        // For simplicity here, we'll use a simple but insecure method.
        // DO NOT use this in a production environment.
        return "hashed_${password}"
    }
}
