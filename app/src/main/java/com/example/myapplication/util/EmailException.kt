package com.example.myapplication.util

/**
 * Thrown when an error occurs during the generation or transmission of classroom reports via email.
 *
 * This exception encapsulates failures in the SMTP communication pipeline, report serialization,
 * or background worker coordination (see [EmailWorker]).
 *
 * @param message A descriptive error message.
 * @param cause The underlying exception that triggered the failure.
 */
class EmailException(message: String, cause: Throwable? = null) : Exception(message, cause)
