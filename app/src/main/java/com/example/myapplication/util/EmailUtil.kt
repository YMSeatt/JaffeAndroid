package com.example.myapplication.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.data.SmtpSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import java.util.regex.Pattern
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

    private val workManager = WorkManager.getInstance(context)

    private fun isValidEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    suspend fun sendEmailWithRetry(
        from: String,
        password: String,
        to: String,
        subject: String,
        body: String,
        attachmentPath: String? = null,
        smtpSettings: SmtpSettings
    ) {
        if (!isValidEmail(from)) {
            throw EmailException("Invalid 'from' email address: $from")
        }
        if (!isValidEmail(to)) {
            throw EmailException("Invalid 'to' email address: $to")
        }
        val data = Data.Builder()
            .putString("request_type", "send_email")
            .putString("from", from)
            .putString("to", to)
            .putString("subject", subject)
            .putString("body", body)
            .putString("attachment_path", attachmentPath)
            .putString("smtp_settings", kotlinx.serialization.json.Json.encodeToString(SmtpSettings.serializer(), smtpSettings))
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EmailWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        workManager.enqueue(workRequest)
    }

    @Throws(EmailException::class)
    suspend fun sendEmail(
        from: String,
        password: String,
        to: String,
        subject: String,
        body: String,
        attachmentPath: String? = null,
        smtpSettings: SmtpSettings
    ) {
        withContext(Dispatchers.IO) {
            val properties = Properties().apply {
                put("mail.smtp.host", smtpSettings.host)
                put("mail.smtp.port", smtpSettings.port.toString())
                put("mail.smtp.auth", "true")
                if (smtpSettings.useSsl) {
                    put("mail.smtp.socketFactory.port", smtpSettings.port.toString())
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                } else {
                    put("mail.smtp.starttls.enable", "true")
                }
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

    companion object {
        private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }
}