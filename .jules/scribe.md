# 📜 Scribe's Journal - Seating Chart & Behavior Logger

## 💡 Tribal Knowledge & Implementation Secrets

### 1. Ghost Cognitive Engine Physics
- **Repulsion Logic**: The `REPULSION_CONSTANT` (500,000f) is calibrated for a 4000x4000 canvas. If the canvas size changes, this constant may need exponential scaling.
- **Social Distancing**: The engine treats negative behavior as a physical repulsion force. A student with 2+ negative marks effectively "pushes" others away 2.5x harder than a neutral student.
- **Equilibrium**: 50 iterations with 0.9 damping was found to be the "sweet spot" where the layout stabilizes without excessive jitter or long wait times.

### 2. AGSL Shader Coordinate Mapping
- **The "Flip"**: Composable `Canvas` coordinates (0,0 at top-left) often need translation when passed to shaders that expect normalized UVs or centered coordinates.
- **Aura Centering**: `iCenter` in `COGNITIVE_AURA` must be passed in absolute pixel coordinates from the Compose `onSizeChanged` or `layout` phase, then normalized *inside* the shader using `iResolution`.

### 3. Voice Assistant Parsing
- **Nicknames over Full Names**: The `handleLogCommand` prioritized nicknames. If a student has a nickname "JJ" and a full name "John Jones", "Log good behavior for JJ" will match more reliably than the full name due to substring matching.
- **Keyword Collision**: Using "bad" in a comment might accidentally trigger a "Negative behavior" log if the parsing logic isn't careful. Current implementation uses simple `contains()`, so teachers should be advised to use specific trigger phrases.

### 4. Neural Map Connections
- **The Star Pattern**: To avoid $O(N^2)$ visual clutter, the `NeuralMapLayer` connects group members in a "star pattern" (everyone to the first member) rather than a "complete graph" (everyone to everyone). This keeps the UI clean while still showing group cohesion.

### 5. Cross-Platform Security & Data Migration
- **The Bridge Key**: The hardcoded key in `SecurityUtil.kt` (Android) and `encryption_key.py` (Python) is the "cryptographic glue" that allows users to move their classroom between desktop and mobile. While Android has upgraded to KeyStore-managed keys (`fernet.key.v2`), this `FALLBACK_KEY` is preserved specifically for data ingestion from the Python app.
- **Hashing Parity**: Python's SHA3-512 hashing was chosen for speed, while Android's PBKDF2 with 100,000 iterations was chosen for security hardening. The Android `verifyPassword` method includes specific logic to detect the length of a hash (128 chars for SHA3-512) and apply the correct legacy verification to ensure users don't get locked out when migrating.

### 6. Database Migration Strategy
- **Relational Groups**: Transitioning `groupId` from `String` to `Long` (v6 to v7) was critical for supporting Room's `@ForeignKey` constraints. This ensures that deleting a `StudentGroup` either cascades or nullifies member students.
- **JSON Flexibility**: The `marksData` field in `QuizLog` (v12) and `HomeworkLog` (v11) is a strategic "future-proofing" move. Instead of adding new columns for every possible scoring metric (effort, accuracy, etc.), developers can store them in a JSON map, processed by the `Converters` class.

### 7. Ghost Catalyst Kinetics
- **The Metaphor**: Classroom behavior is modeled as a chemical system where student actions act as catalysts.
- **Spatio-temporal Inversion**: `GhostCatalystEngine` processes DESC-sorted database logs in reverse to scan chronologically, identifying localized chain reactions within a 300s/800-unit window.
- **Normalization**: Macroscopic metrics like "Reaction Rate" are normalized to a 5-minute window for cross-classroom comparison.
- **Activation Energy**: This heuristic represents the classroom's "resistance" to social volatility. High global frequency (engagement) lowers the activation energy required for a chain reaction.

### 8. Ghost Ion "Neural Ionization"
- **Thermal Correlation**: The engine uses hardware battery temperature as a proxy for classroom "energy." A 0.3x density boost is applied when the battery temperature rises above 25°C, simulating a "heated" atmosphere.
- **Charge Polarity**: Behavior logs are mapped to an electrostatic charge. "Participating" and "Positive" logs are cationic (+), while "Disruptive" and "Negative" logs are anionic (-).
- **GPU Batching**: To avoid hitting the JNI overhead of multiple `setFloatUniform` calls, all 10 student ion points are batched into a single `float4 iPoints[10]` array.

### 9. Ghost Entropy "Neural Turbulence"
- **The "First-20" Tradeoff**: `GhostEntropyLayer` only samples the first 20 students to calculate the global distortion intensity. This is a deliberate performance cap to ensure 60fps on mid-range hardware.
- **Academic Normalization**: The variance normalization uses a 4.0x multiplier because the maximum possible variance of a value in the range [0, 1] is 0.25.
- **Shannon Normalization**: Behavioral entropy is normalized against `ln(5)`, assuming a maximum of 5 distinct behavior types in the standard classroom log schema.

### 10. Ghost Helix "Neural DNA"
- **The Genetic Mapping**: Behavioral and academic data are mapped to four bases: Adenine (Positive Behavior), Thymine (Negative Behavior), Cytosine (High Academic >60%), and Guanine (Low Academic <60%).
- **Twist Dynamics**: The `twistRate` (1.0..3.0) is driven by the frequency of academic logs (C/G), making the helix spin faster for academically active students.
- **Stability Heuristic**: `stability` (0.1..1.0) is calculated as the ratio of positive to total logs. A stability score below 0.5 triggers noticeable procedural jitter in the AGSL shader to visually signal "behavioral noise."

### 11. Ghost Quasar Energy and Polarity
- **Sliding Window**: The engine uses a hardcoded 30-minute window (`30 * 60 * 1000L`). Events older than this are ignored to focus on current classroom "energy."
- **Activation Threshold**: A student only achieves "Quasar" status (and visual auras) after reaching 3 behavioral events within the window. This prevents UI flicker from single, isolated logs.
- **Polarity Mapping**: Polarity is mapped linearly from -1.0 (Magenta/Negative) to +1.0 (Cyan/Positive). The visual core of the accretion disk shifts color based on this normalized float.
- **Shader Batching/Pooling**: To avoid the "Uniform Overwrite" bug (where multiple draw calls sharing the same native shader object overwrite each other's uniforms before the GPU executes), the `GhostQuasarLayer` maintains a `shaderPool`. Each active Quasar is assigned its own `RuntimeShader` instance from the pool.

### 13. Conditional Formatting DSL and Performance
- **Early-Exit Optimization**: Rules with `activeModes` or `activeTimes` are filtered at the start of the `checkCondition` function. This prevents executing more complex behavioral or academic analysis if the rule is logically inactive.
- **Case-Insensitivity & Allocations**: Behavior names in rules are split once into a `Set<String>` during rule decoding. Comparisons use `lowercaseCache` to avoid per-event string allocations when checking against hundreds of historical logs.
- **Sorting Requirement**: The `behavior_count` condition type assumes the `behaviorLog` is sorted in descending chronological order. It uses a manual loop with an early `break` when it hits events outside the `timeWindowHours`, achieving O(Recent) performance instead of O(TotalLogs).

### 12. Ghost Magnetar "Social Polarity"
- **Negativity Bias**: The engine applies a 1.5x multiplier to negative behavioral logs when calculating social magnetic strength. This intentionally models the disproportionate impact disruptive students have on classroom cohesion.
- **Hardware Skewing**: The `iHeading` uniform in `GhostMagnetarLayer` passes the device's actual magnetometer heading. Rotating the physical device rotates the virtual field, a deliberate UI metaphor for "reorienting" social analysis.
- **BOLT Performance Cap**: The shader is hard-coded to 15 dipoles (`iDipolePos[15]`). `GhostMagnetarLayer` uses `students.take(15)` to ensure visual stability and prevent out-of-bounds uniform access in the AGSL program.

### 14. Ghost Vector "Social Gravity" Metrics
- **mG (milli-Gravities)**: The synthetic unit for social force intensity. Calibrated such that a standard interaction at 400 units distance yields approximately 15-60 mG.
- **The 85mG Threshold**: This "High Turbulence" marker is the mathematical equivalent of 1.5x standard deviation in the Python reference suite. It represents a state where social friction outweighs collaboration by a significant margin.
- **Force Multipliers**:
    - `FORCE_FRICTION (-100f)`: Repulsion is weighted 1.6x more heavily than `FORCE_COLLABORATION (60f)` to highlight disruptive social dynamics.
    - `FORCE_NEUTRAL (15f)`: Provides a baseline "social cohesion" pull even in the absence of explicit logs.
- **Zero-Allocation Needle**: The `GhostVectorLayer` utilizes a pooled `RuntimeShader` approach to render hundreds of needles without triggering GC pauses, capturing uniforms just-in-time during the `Canvas` draw pass.

### 15. Ghost Memento Architecture
- **Metaphor**: A specialized "Long-Term Memory" engine for command history persistence.
- **Persistence Strategy**: Uses a high-integrity mapping system (`GhostMementoMapper`) to transform complex production `Command` objects into pure-data `MementoCommand` DTOs.
- **Encryption**: History is serialized to JSON and encrypted using `SecurityUtil` before storage in Jetpack DataStore, ensuring privacy for historical behavioral and academic records.
- **Atomic Recovery**: The `SeatingChartViewModel` re-hydrates its undo/redo stacks from the store on launch, enabling a seamless "Resume Work" experience across app restarts.

### 16. Ghost Osmosis "Neural Diffusion" Calibration
- **The Gaussian Kernel**: The engine uses a Gaussian Radial Basis Function (RBF) for spatial influence, specifically calibrated with **$\sigma = 400$**. This value was chosen to ensure that a student's "potential" remains significant within their immediate group (approx 800 units) but tapers off before affecting the entire 4000-unit canvas.
- **The 320,000 Constant**: To avoid expensive `pow()` and `sqrt()` operations, the code uses a pre-calculated denominator of **320,000f** ($2\sigma^2$).
- **Color Mapping Protocol**:
    - **Blue (Cyan)**: Knowledge Potential (Academic logs).
    - **Green**: Positive Behavioral Concentration.
    - **Red**: Negative Behavioral Concentration.
- **Shader Pooling Logic**: The `GhostOsmosisLayer` uses a strict pooling strategy for `RuntimeShader` and `ShaderBrush`. This is required because `ShaderBrush` captures uniforms at the moment of application; drawing multiple patches with different uniforms in one frame requires unique object instances to prevent visual "bleeding" or overwrites.

### 17. Ghost Pulsar "Harmonic Synchronicity" Calibration
- **The 0.2 Threshold**: The sync threshold for "Harmonic Bonds" is hardcoded to 0.2 LPM. This value was calibrated against the Python reference suite to represent a +/- 12% frequency variance for active students.
- **Phase Derivation**: Phase is calculated as `(currentTime * frequency) % 1.0`. By using absolute system time as the base, students with the *exact* same frequency will naturally pulse in perfect synchronization regardless of when they were added to the chart.
- **Wave Interference**: Visual constructive interference in the AGSL shader occurs when two synchronized students are near each other, with the `sin()` waves combining their amplitudes and shifting colors toward a brighter composite.

### 18. Ghost Flora "Neural Botanical" Calibration
- **The Saturation Point**: The complexity metric is normalized against a **saturation point of 10 logs**. Beyond this, the visual density of petals reaches a maximum to prevent GPU overdraw and maintain 60fps.
- **Academic Baselines**: In the absence of data, students are assigned a **0.75f average growth factor** (0.7 Quiz, 0.8 Homework). This creates a "healthy baseline" sprout rather than an empty canvas for new students.
- **Vitality Color Shift**: The color interpolation in the AGSL shader uses a linear `mix()` between Cyan (Positive) and Magenta (Negative). Magenta was chosen specifically for its visual "friction" against the standard app palette.
- **Stable Seeding**: The procedural seed is derived via `studentId % 1000`. This ensures that while every student has a unique flower, it remains consistent across sessions unless their database ID changes.

### 19. Ghost Mirage Focus Logic
- **The 20x20 Grid Choice**: The grid size was selected as 20x20 (400 cells) to stay within the safe bounds of AGSL uniform array limits (typically 1024 or 2048 floats depending on hardware). This allows passing the entire focus state in a single uniform.
- **Mapping Constant**: The logical 4000x4000 coordinate space is mapped to the 20x20 grid using a `4000 / 20 = 200` unit cell size.
- **Intensity Decay**: The default decay rate of 0.05 units/sec means a full-intensity (1.0) focus zone will completely evaporate in 20 seconds of inactivity.

### 20. Ghost Phasing "Neural Backstage" Synchronization
- **The Metaphor**: The physical classroom UI is treated as a "veil" over a deep-space data void.
- **Haptic Synchronization**: The `GhostPhasingEngine` triggers specific haptic markers at the 10% and 90% phase thresholds. This provides a tactile "pop" when the transition starts and when the physical UI is nearly extinguished.
- **RenderEffect Pipeline**: Unlike standard overlays, Phasing uses `RenderEffect.createRuntimeShaderEffect` on the `graphicsLayer`. This allows the shader to sample the *entire* rendered content of the child Composable, enabling global effects like chromatic aberration and jitter that wouldn't be possible with simple brushes.

### 20. Quiz Scoring & Mark Type Heuristics
- **The "Correct" Dependency**: The `QuizScoreEngine` and `ConditionalFormattingEngine` rely on a specific naming convention to calculate percentages. The engine scans the `QuizMarkType` list for an entry named **"Correct"** (case-insensitive) to establish the base point value for the quiz denominator (`numQuestions * defaultPoints`).
- **BOLT Scoring Context**: To maintain 60fps during bulk updates (where every student icon might trigger a score calculation), the engine utilizes a `QuizScoringContext`. This object pre-calculates mark type HashMaps once and is shared across all students in a single update pass using identity-based memoization (`===`).
- **Legacy Fallback**: If a quiz has 0 questions or no granular mark data, the engine automatically falls back to the legacy `markValue / maxMarkValue` calculation to ensure historical data remains visible.

### 21. Global Performance Tuning via Animation Spec
- **The Pattern**: The application uses a custom `CompositionLocal` named `LocalAnimationSpec` (defined in `Theme.kt`) to globally control the duration and easing of visual transitions.
- **Performance Toggle**: The `SettingsNavHost` and various UI components observe the `noAnimations` preference. When enabled, `LocalAnimationSpec` provides a `tween(durationMillis = 0)`, effectively bypassing Compose's animation overhead for a high-performance "instant" feel.

### 22. Shield Preference Hardening
- **Transparent Security**: The `AppPreferencesRepository` acts as a high-integrity gateway for sensitive settings. It utilizes `securityUtil.decryptSafe()` on read and `securityUtil.encrypt()` on write.
- **Relational Metadata**: Complex preference structures, such as `EmailSchedule` or `SmtpSettings`, are serialized to JSON before encryption, allowing for rich configuration storage within DataStore's flat key-value model.
- **Safe Decryption Migration**: The use of `decryptSafe()` is a critical compatibility pattern that allows the repository to handle unencrypted legacy DataStore entries from older versions while automatically hardening them on the next write cycle.

### 23. Ghost Shell "Neural Pulse" Dynamics
- **Temporal Sensitivity**: The 300,000ms (5-minute) window was selected to ensure the dock reflects *current* classroom state. A longer window would dilute immediate behavioral shifts, while a shorter one would be too volatile.
- **Health Fallback**: In the absence of data, the engine defaults to a **0.7 Health Index**. This "Stable" baseline was chosen to provide a calm visual state (Cyan) rather than an alarming one (Red) for new or quiet sessions.
- **Frequency Calibration**: The pulse frequency scales linearly with activity ($0.5 + \text{count}/20$). A max of 4.0 Hz was found to be the upper limit before the visual flickering became distracting during high-intensity classroom periods.

### 24. UI List Performance (Date Hoisting)
- **The Pattern**: In high-density log lists (Data Viewer), the UI utilizes a "Hoisted Date" pattern.
- **BOLT Optimization**: Instead of allocating a new `Date` object for every row in a `LazyColumn` (which could trigger thousands of allocations during scrolling), a single `Date` is `remember`ed at the top level and its `time` property is updated in-place for each row. This significantly reduces GC pressure and prevents frame drops on mid-range hardware.

### 25. Source Artifact Discovery (The `srcal` Anomaly)
- **The Finding**: A duplicate source directory `app/src/main/srcal` was identified.
- **Tribal Knowledge**: This directory appears to be a legacy artifact or a misconfigured alternative source set. Maintainers should prioritize files in `app/src/main/java` as the primary source of truth, as `srcal` contains outdated versions of core UI components (e.g., `DataViewerScreen.kt` in `srcal` lacks modern security features like `FLAG_SECURE`).

### 26. Ghost Link Dual Implementation
- **Neural Dossier vs. Neural Pairing**: The codebase contains two distinct "Ghost Link" systems.
    - **Dossier** (`labs/ghost/GhostLinkEngine.kt`): Generates a static Markdown report (Dossier) for a single student.
    - **Pairing** (`labs/ghost/link/`): Performs real-time $O(N^2)$ spatial analysis to visualize connections (Strands) between multiple students.
- **Synergy Metrics**: Pairing synergy is based on **parity of activity frequency** within a 10-minute sliding window. High activity in both students leads to a strong link, while a mismatch (e.g., 10 logs vs 0 logs) results in low synergy.
- **Shader Clipping Strategy**: To prevent GPU overdraw while allowing for organic AGSL warping, each link is rendered in a `drawRect` with a **50f padding buffer** beyond the student icon coordinates.

### 27. Ghost Mood Board Synthesis & Thresholds
- **The 15-Minute Window**: The engine analyzes behavioral frequency using a sliding 15-minute window (`15 * 60 * 1000L`).
- **Student State Triggers**:
    - **FOCUSED**: Requires an academic intensity > 0.5 and exactly zero negative logs.
    - **ENERGETIC**: Requires more than 2 positive behavioral logs within the active window.
- **Aggregate Classroom States**:
    - **TURBULENT**: Triggered if > 20% of the students are in a turbulent state.
    - **FOCUSED**: Triggered if > 40% of the students are in a focused state.
    - **ENERGETIC**: Triggered if > 30% of the students are in an energetic state.
- **Stability Logic**: The collective stability is derived as `1.0 - (turbulentCount / studentCount)`, which directly modulates the "neural turbulence" (visual flicker) in the AGSL shader.

### 28. Ghost Ink Spatial Hardening
- **Thinning Threshold**: The engine uses a **25f squared-distance threshold** for point ingestion. This ensures that only meaningful spatial changes are recorded, preventing coordinate bloat in the 4000x4000 logical space.
- **Shader Pooling**: `GhostInkLayer` implements a strict **16-instance limit** for concurrent stroke rendering using a pre-allocated pool of `RuntimeShader` and `ShaderBrush` objects. This is a critical BOLT optimization to maintain 60fps during complex annotation sessions.
- **Logical Anchoring**: Unlike UI overlays, Ink strokes are stored in **chart-logical coordinates**. This allows annotations to persist correctly across zoom and pan states, effectively "sticking" to the classroom floor.

### 29. Ghost Phoenix "Neural Resilience" Calibration
- **The Metaphor**: Classroom resilience is modeled as a "Phoenix" rising from a historical struggle (ashes).
- **The Windows**:
    - **Struggle Window**: 24 hours. Analyzes historical logs to establish a baseline of behavioral friction.
    - **Recovery Window**: 2 hours. Analyzes recent logs to identify a streak of positive engagement.
- **The Formula**: `Score = (RecentPositiveCount * 0.2) + (HistoricalNegativeCount * 0.1)`.
- **Constraint**: Resilience is only calculated if `HistoricalNegativeCount > 0`. A student who has never struggled is not a Phoenix in this model; they are simply "Stable".
- **Visual Trigger**: The "Phoenix Rising" fire aura triggers when the score exceeds the **0.6f threshold**.

## 💡 Ghost Warp "Neural Spacetime" Constants
- **Recency Bias**: The engine applies a **2.0x weight** to behavior logs recorded within the last **60 minutes**. This ensures that the visual "warp" reflects the current classroom atmosphere rather than long-term history.
- **Negativity Polarity**: Negative logs are weighted **1.5x** more heavily than positive ones in the mass calculation. This is a deliberate pedagogical choice to highlight areas of social friction in the spacetime grid.
- **Shader Smoothing**: The `NEURAL_WARP` shader utilizes a **0.1 softening factor** to prevent mathematical singularities at the center of student nodes.
- **Cap**: The system is hard-coded to a maximum of **10 concurrent gravity wells** for performance stability.

---
*Documentation love letter from Scribe 📜*
