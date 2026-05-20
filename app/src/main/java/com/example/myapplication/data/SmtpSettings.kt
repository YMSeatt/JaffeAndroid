package com.example.myapplication.data

import kotlinx.serialization.Serializable

/**
 * Configuration metadata for the Simple Mail Transfer Protocol (SMTP) server.
 *
 * This data class encapsulates the technical parameters required to establish
 * a secure connection with an external mail server (e.g., Gmail, Outlook).
 *
 * ### Security Context:
 * These settings are utilized by [com.example.myapplication.util.EmailUtil] to configure
 * the JavaMail session. The application enforces modern security standards, including
 * mandatory STARTTLS and server identity verification, to protect the integrity
 * of classroom report transmissions.
 *
 * @property host The fully qualified domain name of the SMTP server (e.g., "smtp.gmail.com").
 * @property port The port number used for the connection (typically 587 for STARTTLS or 465 for SSL).
 * @property useSsl Whether to use legacy implicit SSL (True) or modern STARTTLS (False).
 */
@Serializable
data class SmtpSettings(
    val host: String = "smtp.gmail.com",
    val port: Int = 587,
    val useSsl: Boolean = false
)
