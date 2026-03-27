# ⚙️ User Preferences & Application Configuration

This package manages the application's persistent configuration and user settings, providing a centralized and reactive source of truth for the UI and background services.

## 🏛️ Architecture & Persistence

The configuration layer is built on **Jetpack DataStore**, a modern data storage solution that replaces SharedPreferences.

-   **`AppPreferencesRepository`**: The primary coordinator. It manages the DataStore instance, provides reactive streams (`Flow`) for all settings, and exposes atomic `suspend` methods for updates.
-   **`UserPreferences`**: A unified, immutable state container. This data class aggregates all individual settings into a single object, allowing the UI to observe and react to configuration changes as a cohesive unit.
-   **Unidirectional Data Flow (UDF)**: Settings are updated via explicit repository methods and observed through `Flow` streams, ensuring that the UI always reflects the current persistent state.

## 🛡️ Shield (Security Boundary)

Adhering to the **Shield** philosophy, the preferences layer acts as a critical security boundary for sensitive user information.

-   **Transparent Encryption**: The `AppPreferencesRepository` integrates with `SecurityUtil` to encrypt sensitive fields at rest. This includes SMTP passwords, email addresses, custom behavior/homework initials, and export paths.
-   **`decryptSafe()` Migration**: The repository utilizes "safe decryption" logic to handle unencrypted legacy data, automatically hardening the storage as settings are accessed or updated.
-   **Hardware-Backed Protection**: When paired with `SecurityUtil`, encryption leverages the Android KeyStore for hardware-level security (on supported devices), ensuring that sensitive configuration cannot be easily extracted.

## ⚡ Performance & Reactivity

-   **Reactive Flows**: All settings are exposed as `Flow<T>`, enabling the Jetpack Compose UI to perform fine-grained recompositions only when relevant configuration changes.
-   **Multi-Field Aggregation**: The `userPreferencesFlow` utilizes the `combine` operator to merge dozens of individual setting streams into a single, high-level observation point, simplifying state management in ViewModels.
-   **Asynchronous Commit**: Updates are performed off the main thread using DataStore's coroutine-based API, ensuring that the UI remains responsive even during disk I/O.

---
*Documentation love letter from Scribe 📜*
