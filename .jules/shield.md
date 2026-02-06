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
