package com.example.myapplication.data

import kotlinx.serialization.Serializable

@Serializable
data class SmtpSettings(
    val host: String = "smtp.gmail.com",
    val port: Int = 587,
    val useSsl: Boolean = false
)
