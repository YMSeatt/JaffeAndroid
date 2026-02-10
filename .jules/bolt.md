# BOLT'S JOURNAL - PERFORMANCE OPTIMIZATIONS

## O(N*M) Data Processing in ViewModels
- **Discovery:** `SeatingChartViewModel.updateStudentsForDisplay` was filtering flat lists of session logs (quiz and homework) for every student inside the main mapping loop.
- **Fix:** Pre-grouped logs by `studentId` using `groupBy` before entering the loop, transforming O(S * L) complexity into O(S + L).
- **Impact:** Significant reduction in UI latency during high-frequency updates (e.g., student dragging).

## Redundant Object Allocations in Compose
- **Discovery:** Multiple screens and dialogs (`BehaviorLogViewerDialog`, `StatsScreen`, etc.) were creating new `SimpleDateFormat` instances on every recomposition or for every item in a `LazyColumn`.
- **Fix:** Wrapped `SimpleDateFormat` in `remember { ... }` blocks and moved creation out of item loops.
- **Impact:** Reduced GC pressure and improved scroll smoothness.

## Inefficient Group Lookup
- **Discovery:** Student group color lookup used `groups.find { ... }` inside the student loop.
- **Fix:** Pre-mapped groups to their colors using `associate` before the loop for O(1) lookup.
- **Impact:** Micro-optimization for large student lists.

## Optimized Excel Export and JSON Parsing
- **Discovery:** `Exporter.kt` was repeatedly parsing `marksData` JSON for every student across multiple sheets, creating thousands of redundant `Date` and `Calendar` objects, and using inefficient functional chains for dynamic header collection.
- **Fix:**
    - Implemented `parsedMarksCache` (MutableMap) to reuse JSON deserialization results.
    - Reused `Date` and `Calendar` instances in `FormattingContext`, updating `date.time` in loops.
    - Refactored dynamic header collection to use a single-pass loop with a `MutableSet` and O(1) lookups for known keys.
- **Impact:** Dramatically reduced export time and GC pressure for large datasets.

## Modern Enum Access
- **Discovery:** `HomeworkTemplateEditDialog.kt` used `Enum.values()` inside a Compose loop, which allocates a new array on every call.
- **Fix:** Switched to `Enum.entries` (Kotlin 1.9+).
- **Impact:** Eliminated unnecessary allocations in UI code.
