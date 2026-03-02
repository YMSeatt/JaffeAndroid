# 🧠 Developer Insights: Tribal Knowledge & Implementation Secrets

This document consolidates the critical "Tribal Knowledge" and technical secrets that drive the Seating Chart & Behavior Logger ecosystem. These insights are essential for maintainers looking to modify core engines or ensure cross-platform compatibility.

## 🌌 Ghost Cognitive Engine Physics

The `GhostCognitiveEngine` (Android) utilizes a force-directed graph algorithm to optimize classroom layouts.

- **Repulsion Constant**: The `REPULSION_CONSTANT` (500,000f) is precisely calibrated for a **4000x4000** logical canvas.
    - *Warning*: If the canvas size changes, this constant may require exponential scaling to maintain stability.
- **Social Distancing Multiplier**: Negative behavior logs exert a physical repulsion force. A student with 2 or more negative marks "pushes" others away **2.5x harder** than a neutral student.
- **Equilibrium Sweet Spot**: Through testing, **50 iterations** with **0.9 damping** was identified as the ideal configuration to achieve layout stability without excessive jitter or calculation overhead.

## 🎨 AGSL Shader Coordinate Mapping

The application's futuristic visualizations rely on high-performance AGSL shaders, which require careful coordinate translation.

- **The "Flip"**: Android's `Canvas` (Compose) uses (0,0) at the top-left. Many shaders expect normalized UVs (0 to 1) or centered coordinates (-1 to 1).
- **Aura Centering**: In the `COGNITIVE_AURA` shader, the `iCenter` uniform must be passed in **absolute pixel coordinates** (calculated during the Compose `layout` or `onSizeChanged` phase). The shader then normalizes this internally using the `iResolution` uniform.

## 🎙️ Voice Assistant Parsing Logic

The `GhostVoiceAssistant` provides hands-free management but has specific behavioral quirks.

- **Student Matching**: The parser scans for both student **Full Names** and **Nicknames**.
    - *Prioritization*: While the code matches based on first-found in the student list, nicknames are often more reliable for speech recognition as they are typically shorter and less prone to phonetic misinterpretation.
- **Keyword Collision Risk**: The behavior type parser uses simple substring matching (`contains()`).
    - *Example*: If a teacher says "Log a quick note: student was NOT bad", the word **"bad"** might accidentally trigger a "Negative behavior" log.
    - *Mitigation*: Teachers are encouraged to use specific trigger phrases like "Positive Participation" or "Asked Question" and keep comments concise.

## 🕸️ Neural Map Connections

To maintain visual clarity in the `NeuralMapLayer`:
- **The Star Pattern**: Instead of drawing a "complete graph" ($O(N^2)$ lines) connecting every group member to every other member, the engine connects everyone to the **first member** of the group in a "star pattern". This visually conveys group cohesion while minimizing UI clutter.

## 🔐 Cross-Platform Security Bridge

Ensuring data sync between the Python desktop app and the Android mobile app requires cryptographic parity.

- **The Bridge Key**: `SecurityUtil.kt` (Android) contains a `FALLBACK_KEY` that matches the hardcoded key in Python's `encryption_key.py`. This serves as the "cryptographic glue" for ingestion. Android will automatically migrate ingested data to its hardened, KeyStore-managed key (`fernet.key.v2`).
- **Hash Detection**: Python uses **SHA3-512** (128-char hex strings) for passwords, while Android uses **PBKDF2**. The Android `verifyPassword` method detects the length of the stored hash; if it is 128 characters, it applies the legacy SHA3-512 verification to prevent user lockout during migration.

## 🐍 Python File Splitting

The primary Python script `seatingchartmain.py` (approx. 400KB) is physically split into `First Half.py` and `Second half.py`.
- **Rationale**: This was done to circumvent legacy file size limitations in certain deployment or editing environments.
- **Maintenance**: Both "half" files should be kept in sync with the combined `seatingchartmain.py`, as they represent the same logical application state.

## 🗄️ Database Schema Evolution

The application's Room database has evolved through numerous architectural shifts to support increasing complexity.

- **Relational Hardening**: The schema transitioned from simple string-based group assignments (v6) to a robust relational model with `Long` primary keys (v7/8). This ensures data integrity when students are moved between groups or deleted.
- **JSON-Backed Flexibility**: To avoid frequent schema migrations for UI-driven changes, log entities (`HomeworkLog` v11, `QuizLog` v12) utilize JSON-based `marksData` fields. This allows the application to support dynamic scoring types (e.g., "Partial Credit", "Effort Marks") without altering the underlying SQLite tables.
- **Automated Lifecycle**: Modern versions introduce automated subsystems like `Reminder` (v23) and `EmailSchedule` (v24), which operate as independent background workers driven by specific database triggers.

---
*Documentation love letter from Scribe 📜*
