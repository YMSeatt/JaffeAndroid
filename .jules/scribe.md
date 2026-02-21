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
