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
