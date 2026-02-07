# SHIELD'S JOURNAL - CRITICAL LEARNINGS

## 🔦 Insecure SMTP Configuration (Hardened)
- **Vulnerability:** Downgrade attacks (STRIPTLS) and MITM attacks via mismatched certificates.
- **Fix:**
    - Enforced `mail.smtp.starttls.required = true` to prevent fallback to plain text if STARTTLS is stripped.
    - Enabled `mail.smtp.ssl.checkserveridentity = true` to ensure the server's certificate matches the hostname.
- **Location:** `app/src/main/java/com/example/myapplication/util/EmailUtil.kt`

## 🛡️ Best Practices
- Always verify server identity for any SSL/TLS connection.
- Require STARTTLS explicitly if the transport depends on it for security.

## 🛡️ Data Hardening: PII and Password Encryption
- **Vulnerability:** Recipient emails, subjects, and bodies in `EmailSchedule` and `PendingEmail` were stored in plain text in the local Room database. The application password hash was stored in plain text in DataStore.
- **Fix:**
    - Implemented `EmailRepository` to handle transparent encryption/decryption of PII in Room entities using `SecurityUtil`.
    - Hardened `AppPreferencesRepository` to encrypt the `PASSWORD_HASH` in DataStore.
    - Updated `EmailSchedulesViewModel`, `EmailSchedulerWorker`, and `EmailWorker` to use the secure repository.
- **Location:** `app/src/main/java/com/example/myapplication/data/EmailRepository.kt`, `app/src/main/java/com/example/myapplication/preferences/AppPreferencesRepository.kt`
