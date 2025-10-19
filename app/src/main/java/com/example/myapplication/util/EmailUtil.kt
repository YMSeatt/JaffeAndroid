package com.example.myapplication.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailUtil(private val context: Context) {

    private val TAG = "EmailUtil"

    suspend fun sendEmailWithRetry(
        from: String,
        password: String,
        to: String,
        subject: String,
        body: String,
        attachmentPath: String? = null,
        retries: Int = 3
    ) {
        var currentRetry = 0
        while (currentRetry < retries) {
            try {
                sendEmail(from, password, to, subject, body, attachmentPath)
                Log.i(TAG, "Email sent successfully to $to")
                return // Success
            } catch (e: EmailException) {
                Log.e(TAG, "Error sending email to $to: ${e.message}", e)
                currentRetry++
                if (currentRetry < retries) {
                    Log.d(TAG, "Retrying to send email to $to in 5 seconds... ($currentRetry/$retries)")
                    delay(5000)
                } else {
                    Log.e(TAG, "Failed to send email to $to after $retries retries.")
                    throw e // Re-throw after final retry
                }
            }
        }
    }

    @Throws(EmailException::class)
    suspend fun sendEmail(
        from: String,
        password: String,
        to: String,
        subject: String,
        body: String,
        attachmentPath: String? = null
    ) {
        if (!EmailValidator.isValidEmail(from)) {
            throw EmailException("Invalid 'from' email address: $from")
        }
        if (!EmailValidator.isValidEmail(to)) {
            throw EmailException("Invalid 'to' email address: $to")
        }

        withContext(Dispatchers.IO) {
            val properties = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(from, password)
                }
            })

            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(from))
                    addRecipient(Message.RecipientType.TO, InternetAddress(to))
                    setSubject(subject)
                }

                val multipart = MimeMultipart()

                val messageBodyPart = MimeBodyPart().apply {
                    setContent(body, "text/html; charset=utf-8")
                }
                multipart.addBodyPart(messageBodyPart)

                attachmentPath?.let {
                    val attachmentBodyPart = MimeBodyPart().apply {
                        val source = FileDataSource(it)
                        dataHandler = DataHandler(source)
                        fileName = source.name
                    }
                    multipart.addBodyPart(attachmentBodyPart)
                }

                message.setContent(multipart)

                Transport.send(message)
            } catch (e: AddressException) {
                throw EmailException("Invalid email address.", e)
            } catch (e: MessagingException) {
                throw EmailException("Error sending email.", e)
            } catch (e: Exception) {
                throw EmailException("An unexpected error occurred.", e)
            }
        }
    }
}
