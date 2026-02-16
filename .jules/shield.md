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

## 🛡️ Privacy Hardening: Screenshot PII Protection
- **Vulnerability:** Seating chart screenshots containing student PII (names, layouts) were saved to the public `DIRECTORY_PICTURES` folder, making them accessible to any app with media permissions.
- **Fix:**
    - Redirected screenshot storage to the app's internal cache directory.
    - Implemented secure sharing via `FileProvider` and `Intent.ACTION_SEND`, ensuring PII is only shared explicitly by the user.
- **Location:** `app/src/main/java/com/example/myapplication/viewmodel/SettingsViewModel.kt`, `app/src/main/java/com/example/myapplication/ui/screens/SeatingChartScreen.kt`

## 🛡️ Network Hardening: Cleartext Traffic Disabled
- **Vulnerability:** Cleartext (HTTP) traffic was not explicitly disabled, which could allow insecure network communication on older Android versions or if misconfigured.
- **Fix:**
    - Implemented `network_security_config.xml` with `cleartextTrafficPermitted="false"` for the entire application.
    - Referenced the configuration in `AndroidManifest.xml`.
- **Location:** `app/src/main/res/xml/network_security_config.xml`, `app/src/main/AndroidManifest.xml`

## 🛡️ Observability and Security: Improved Error Logging
- **Vulnerability:** Use of `e.printStackTrace()` in `SettingsViewModel.kt` could leak sensitive internal application state or paths to standard output/logs.
- **Fix:**
    - Replaced `e.printStackTrace()` with structured `Log.e` calls.
- **Location:** `app/src/main/java/com/example/myapplication/viewmodel/SettingsViewModel.kt`

## 🛡️ Key Hardening: Android KeyStore Integration
- **Vulnerability:** The core 32-byte Fernet encryption key was stored in plain text in the app's internal storage (`fernet.key`), making it extractable on rooted devices or via insecure backups.
- **Fix:**
    - Integrated `androidx.security:security-crypto` to leverage the `AndroidKeyStore`.
    - Implemented `MasterKey` wrapping for the Fernet key, now stored in `fernet.key.v2` (encrypted with AES-GCM).
    - Added an automatic migration path from the legacy plain-text key to the hardened format.
    - Enforced KeyStore usage in production while allowing a safe, unencrypted fallback only in detected unit test environments (e.g., Robolectric).
- **Location:** `app/src/main/java/com/example/myapplication/util/SecurityUtil.kt`

## 🛡️ Data Hardening: Comprehensive DataStore Encryption
- **Vulnerability:** Several fields containing student PII or potentially sensitive classroom information (e.g., `EMAIL_SCHEDULES`, `LAST_QUIZ_NAME`, `BEHAVIOR_INITIALS_MAP`) were stored in plain text in DataStore.
- **Fix:**
    - Hardened `AppPreferencesRepository.kt` by wrapping all sensitive fields in encryption/decryption logic.
    - Utilized `securityUtil.decryptSafe()` to ensure zero-friction migration for existing unencrypted data.
- **Location:** `app/src/main/java/com/example/myapplication/preferences/AppPreferencesRepository.kt`

## 🛡️ Backup Hardening: Encryption Key Exclusion
- **Vulnerability:** The hardened encryption key (`fernet.key.v2`) was not explicitly excluded from Android's auto-backup system, potentially allowing the key to be backed up to the cloud in an extractable state.
- **Fix:**
    - Updated `data_extraction_rules.xml` and `backup_rules.xml` to explicitly exclude both legacy and hardened key files from cloud backups and device transfers.
- **Location:** `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/res/xml/backup_rules.xml`

## 🛡️ Privacy Hardening: Secure Cleanup of Exported Reports
- **Vulnerability:** Temporary Excel files containing sensitive student PII (names, behavior logs, quiz scores) were left in the app's cache directory after being emailed, potentially allowing local data exposure.
- **Fix:**
    - Implemented `try-finally` blocks in `EmailWorker.kt` to ensure that any temporary report file created for emailing is deleted immediately after the email is sent or if the operation fails.
    - Specifically targeted `daily_report.xlsx`, `on_stop_export.xlsx`, and general attachment paths passed from the UI.
- **Location:** `app/src/main/java/com/example/myapplication/util/EmailWorker.kt`
