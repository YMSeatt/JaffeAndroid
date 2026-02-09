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
