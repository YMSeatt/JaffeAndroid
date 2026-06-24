# 🌊 Ghost Stream: Neural Activity Ticker

Ghost Stream is an experimental, real-time activity ticker that synthesizes disparate classroom data streams (Behavioral, Quiz, and Homework logs) into a unified, glassmorphic chronological feed. It provides teachers with a "Neural Stream" of classroom interactions, enabling high-fidelity awareness of classroom tempo and student engagement.

## 🏛️ Architectural Components

The Stream system is composed of three primary elements:

1.  **`GhostStreamEngine` (The Brain)**: A high-performance synthesis engine that aggregates and formats events from multiple Room DAOs. It implements complex sorting and filtering logic to maintain a consistent chronological feed.
2.  **`GhostStreamShader` (The Visuals)**: An AGSL (Android Graphics Shading Language) program that renders the ticker's background. It features a "Neural Flow" animation with scrolling data lines and procedural noise, visualizing the velocity of classroom data.
3.  **`GhostStreamLayer` (The Interface)**: A Jetpack Compose overlay that provides a glassmorphic, scrolling vertical ticker. It utilizes `AnimatedVisibility` and `LazyColumn` with identity-preserving keys for fluid entry/exit animations.

## 📐 Synthesis Logic & Models

### 1. Unified Event Mapping
The engine maps different academic and behavioral entities into a common `StreamEntry` model:
-   **Behavior Events**: Categorized into `POSITIVE` (Cyan) or `NEGATIVE` (Magenta) based on type metadata.
-   **Quiz & Homework Logs**: Consolidated into an `ACADEMIC` (Purple) stream, displaying scores and status updates.
-   **System Events**: Reserved for pedagogical milestones or engine status updates.

### 2. Temporal Formatting
Events are timestamped and formatted using the system's local timezone (HH:mm:ss). The ticker uses absolute `Instant` timestamps to ensure that even events from disparate database tables are interleaved with millisecond precision.

## ⚡ BOLT Performance Optimizations

Ghost Stream is designed for zero-lag performance even in high-density classrooms with thousands of historical logs:

-   **O(maxEntries) Complexity**: The engine leverages pre-sorted DAO inputs to only process the first $N$ items of each list. This transforms a potentially $O(L)$ operation into a fixed $O(\text{maxEntries})$ pass, ensuring sub-millisecond synthesis.
-   **Deferred Formatting**: Expensive string concatenations, ID generation, and [StudentUiItem] lookups are deferred until *after* the final top-N items have been identified.
-   **LongSparseArray Lookups**: The engine pre-indexes students into a native `android.util.LongSparseArray`, transforming student name resolution from $O(\text{maxEntries} \times S)$ to $O(S + \text{maxEntries})$.
-   **Zero-Allocation Draw Loop**: The `GhostStreamLayer` hoists the `RuntimeShader` and `ShaderBrush` using `remember`, preventing per-frame object churn during 60fps ticker animations.

---
*Documentation love letter from Scribe 📜*
