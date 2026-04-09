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

## AGSL Shader Lifecycle & Spatial Lookup Optimizations
- **Discovery:** Multiple experimental "Ghost" layers were re-instantiating `RuntimeShader` on every frame (or even inside loops, in the case of `GhostLatticeLayer`). Additionally, `GhostLatticeLayer` was performing $O(N)$ list searches inside a draw loop.
- **Fix:**
    - Wrapped `RuntimeShader` instantiation in `remember` blocks (or `drawWithCache` where appropriate) across all layers.
    - Implemented a student ID-to-Item map (`associateBy`) in `GhostLatticeLayer` to transform O(E * S) lookups into O(E).
    - Optimized `StringSimilarity.levenshteinDistance` to reuse two `IntArray` rows, eliminating per-row allocations.
- **Impact:** Significant reduction in object churn and GC pressure, ensuring 60fps performance even when multiple experimental visualizers are active.

## High-Frequency Particle Physics and Allocation Optimization
- **Discovery:** The `GhostSparkLayer` was allocating 8 `FloatArray`s and a `List` (`take(100)`) per frame, creating significant GC pressure. Additionally, `GhostSparkEngine` physics performed expensive `sqrt` calls and repeated `MutableState` reads in its $O(S \times N)$ nested loop.
- **Fix:**
    - Implemented a `Spark` object pool to eliminate per-event allocations.
    - Pre-allocated and `remember`ed `FloatArray` buffers for shader uniforms.
    - Replaced `SnapshotStateList` with `ArrayList` for physics-only state.
    - Eliminated $O(S \times N)$ `sqrt` calls by refactoring force calculations to use squared distances.
    - Pre-fetched `MutableState` student positions into local arrays for the physics loop.
    - Collapsed multiple `filter`/`count` passes in `GhostAuroraEngine` into a single-pass loop.
- **Impact:** Eliminated per-frame allocations in the particle system and significantly reduced CPU overhead for physics updates, enabling stable 60fps performance for experimental visualizations.

## Ghost Lab Layer Drawing Optimizations
- **Discovery:** Several experimental Ghost layers (`GhostHologramLayer`, `GhostTectonicLayer`, `GhostVisionLayer`) were performing expensive object allocations (like `RuntimeShader`, `ShaderBrush`, or `FloatArray`) directly inside their `Canvas` draw loops or on every recomposition. `GhostVisionLayer` also used iterator-based loops for student projections.
- **Fix:**
    - Hoisted `RuntimeShader` and `ShaderBrush` creation into `drawWithCache` or `remember` blocks.
    - Pre-allocated and `remember`ed `FloatArray` buffers (e.g., `nodeData` in Tectonics) and ensured they are reset via `fill(0f)` to prevent stale data artifacts.
    - Replaced `forEach` student loops with manual index-based `for` loops in high-frequency projection code.
- **Impact:** Significant reduction in frame-time jitter and GC pressure during experimental mode activation, ensuring smooth 60fps interaction even with complex AGSL visualizers.
