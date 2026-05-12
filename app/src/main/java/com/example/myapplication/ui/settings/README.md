# ⚙️ Application Settings & Configuration UI

This package contains the user interface for managing application preferences, data migration, and classroom configuration. It implements a tab-based, navigable settings hub that bridges user intent with the persistent state managed by the [Preferences Layer](../../preferences/README.md).

## 🏛️ Architecture

The settings UI is organized around a central navigation hub and a multi-tab main screen:

### 1. The Navigation Hub (`SettingsNavHost.kt`)
Utilizes **Jetpack Navigation** to manage the flow between the main settings tabs and specialized management screens (e.g., Student Groups, Quiz Templates, Reminders).
- **Performance-Aware Transitions**: Integrates with the `noAnimations` preference to globally toggle screen transitions (`EnterTransition.None` / `ExitTransition.None`) via a custom `LocalAnimationSpec`.

### 2. The Settings Hub (`SettingsScreen.kt`)
Implements a 5-tab interface using a `HorizontalPager` and `TabRow` to organize dozens of configuration options:

- **General**: Security (Passwords), Language, and core application behavior.
- **Display**: Visual customization for the seating chart, including student icon dimensions and the "Manage Initials" workflow.
- **Data**: Management of homework templates, database archiving, and data restore operations.
- **Advanced**: Access to relational management screens like Student Groups, Conditional Formatting, and Automated Email Schedules.
- **SMTP**: Configuration for outbound report delivery via SMTP.

## ⚡ Performance Patterns (BOLT)

To ensure that the settings UI remains responsive—even when handling encrypted data or heavy navigation transitions—the following patterns are used:

-   **Global Animation Control**: Uses a `CompositionLocal` (`LocalAnimationSpec`) to allow users to disable animations across the entire settings hierarchy. This is particularly effective for users on lower-end devices or those preferring a high-speed, "no-frills" interface.
-   **Lazy List Optimization**: Tab contents (e.g., `AdvancedSettingsTab`) use `LazyColumn` to ensure that scrolling through numerous buttons and toggles remains fluid.
-   **Scoped ViewModels**: Specialized screens like `ConditionalFormattingScreen` use dedicated ViewModels to isolate logic and prevent the main `SettingsViewModel` from becoming a monolithic "God Object."

## 🛡️ Security & Privacy (Shield)

The settings layer is the primary interface for managing the application's **Shield** security features:

-   **Transparent Encryption**: All sensitive fields (SMTP passwords, export paths, email addresses) are transparently handled. The UI interacts with decrypted strings, while the `AppPreferencesRepository` ensures they are encrypted before touching the disk.
-   **Password Hardening**: Manages the multi-format password system, supporting both modern PBKDF2 hashes and legacy SHA3-512 hashes for cross-platform compatibility with the Python desktop app.
-   **Secure Deletion**: Data management tools (like "Clear Logs") implement confirmation dialogs and atomic database operations to prevent accidental data loss.

---
*Documentation love letter from Scribe 📜*
