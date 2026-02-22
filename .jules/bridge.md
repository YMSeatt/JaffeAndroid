# BRIDGE'S JOURNAL - CRITICAL LEARNINGS

## 2025-01-24 - Inactivity Auto-Lock Pattern
**Discrepancy:** The Python prototype uses a manual `check_auto_lock` method called periodically or on specific events. Android/Compose doesn't have a direct equivalent of a global event listener for "any activity" that is easy to hook into.
**Adaptation:** Used `Activity.onUserInteraction()` to track the last activity timestamp at the Activity level. In Compose, a `LaunchedEffect` keyed on this timestamp acts as a self-resetting timer: every time the timestamp updates, the effect restarts, effectively delaying the lock action.

```kotlin
// MainActivity.kt
private var lastActivityTime by mutableStateOf(System.currentTimeMillis())

override fun onUserInteraction() {
    super.onUserInteraction()
    lastActivityTime = System.currentTimeMillis()
}

// Inside setContent
LaunchedEffect(autoLockEnabled, autoLockTimeoutMinutes, unlocked, lastActivityTime) {
    if (autoLockEnabled && unlocked && autoLockTimeoutMinutes > 0) {
        delay(autoLockTimeoutMinutes * 60 * 1000L)
        unlocked = false
    }
}
```

## 2025-01-24 - Security Parity vs Safety
**Discrepancy:** The Python prototype includes a hardcoded master recovery password hash.
**Adaptation:** While porting for parity, it was decided (after review) to omit the master recovery password hash in the Android implementation to avoid introducing a security backdoor, prioritizing platform security best practices over strict logical parity for sensitive features.

## 2026-02-07 - Attendance Report Feature Port
**Discrepancy:** The Python application uses a separate logic block and dialog for generating attendance reports, inferring presence from log activity. The Android application had Excel export but lacked this specific "inferred attendance" logic.
**Adaptation:** Integrated the attendance report generation directly into the existing `Exporter` infrastructure in Android. Added `includeAttendanceSheet` to `ExportOptions` and implemented `createAttendanceSheet` in `Exporter.kt`, using Kotlin's `any` for efficient presence checks across multiple log types.

## 2026-02-22 - Social Cohesion Analysis Port
**Discrepancy:** The Python prototype uses a standalone script (`ghost_vector_analysis.py`) to generate Markdown reports and calculate student "Social Status" based on net force magnitudes. The Android application had the basic vector math in `GhostVectorEngine.kt` but lacked the categorization and reporting logic.
**Adaptation:** Ported the classification thresholds (85/40/5) and global cohesion logic (threshold 50.0) into `GhostVectorEngine.kt`. Implemented `generateSocialReport` using `StringBuilder` and `Locale.US` to ensure floating-point formatting parity (dots vs commas) regardless of device locale, matching Python's default behavior.
