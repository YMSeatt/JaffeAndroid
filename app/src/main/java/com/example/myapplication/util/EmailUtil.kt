package com.example.myapplication.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailUtil(private val context: Context) {

    suspend fun sendEmail(
        from: String,
        password: String,
        to: String,
        subject: String,
        body: String,
        attachmentPath: String? = null
    ) {
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
                    setText(body)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
