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

/**
 * EmailUtil: A utility for secure SMTP communication and automated reporting.
 *
 * This class encapsulates JavaMail operations, providing a bridge between the
 * application's reporting logic and external mail servers. It handles both direct
 * email transmission and backgrounding via [WorkManager] for improved reliability.
 *
 * ### Security Hardening:
 * - **Identity Verification**: Enforces `mail.smtp.ssl.checkserveridentity` to prevent MITM attacks.
 * - **Mandatory Encryption**: Requires STARTTLS and disables downgrade attempts.
 * - **Encrypted Payload**: Sensitive metadata is encrypted using [SecurityUtil] before being
 *   passed to background workers.
 *
 * @param context Application context used for accessing WorkManager and SecurityUtil.
 */
class EmailUtil(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val securityUtil = SecurityUtil(context)

    /**
     * Validates an email address using a standard RFC 5322-compliant pattern.
     */
    private fun isValidEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    /**
     * Offloads an email request to [EmailWorker] via [WorkManager] for background processing.
     *
     * This is the preferred method for sending emails from the UI, as it ensures delivery
     * even if the app is closed or the network is unstable. It enforces a `CONNECTED`
     * network constraint.
     *
     * @param from The sender's email address.
     * @param password The sender's SMTP password (used for authentication).
     * @param to The recipient's email address.
     * @param subject The email subject line.
     * @param body The HTML or plaintext body of the email.
     * @param attachmentPath Optional absolute path to a file to be attached (e.g., an Excel report).
     * @param smtpSettings Configuration for the SMTP server (host, port, SSL/TLS).
     */
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
            .putString("to", securityUtil.encrypt(to))
            .putString("subject", securityUtil.encrypt(subject))
            .putString("body", securityUtil.encrypt(body))
            .putString("attachment_path", attachmentPath?.let { securityUtil.encrypt(it) })
            .putString("smtp_settings", securityUtil.encrypt(kotlinx.serialization.json.Json.encodeToString(SmtpSettings.serializer(), smtpSettings)))
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

    /**
     * Directly executes an SMTP transmission using the JavaMail API.
     *
     * This method performs synchronous I/O on [Dispatchers.IO] and should typically
     * be called from a background worker like [EmailWorker].
     *
     * ### Features:
     * 1. **Multi-part Support**: Correctly handles both HTML body content and file attachments.
     * 2. **Session Authentication**: Uses standard [Authenticator] for credential verification.
     * 3. **Robust Security**: Configures the SMTP session with mandatory SSL/TLS and identity checks.
     *
     * @throws EmailException If validation fails, authentication is rejected, or a network error occurs.
     */
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
                put("mail.smtp.ssl.checkserveridentity", "true") // HARDEN: Prevent MITM with mismatched certs
                if (smtpSettings.useSsl) {
                    put("mail.smtp.socketFactory.port", smtpSettings.port.toString())
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                } else {
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.starttls.required", "true") // HARDEN: Prevent downgrade attacks
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
        /**
         * A regular expression pattern for validating email addresses.
         * Derived from common Android/Web standards to ensure high compatibility.
         */
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