# 🛰️ Ghost Tiles: Quick Settings Integration

A Proof of Concept (PoC) for a future where classroom management is truly hands-free and "eyes-free." **Ghost Tiles** brings essential classroom controls and behavioral logging directly into the Android system's Quick Settings panel.

## 🌟 The Vision
In 2027, teachers shouldn't be tethered to their device's screen. If a student exhibits positive behavior while the teacher is walking around the room, they should be able to reward that behavior without looking away from the class or unlocking their phone. Ghost Tiles enables this through native Android Quick Settings integration.

## 🛠️ The Tech
- **TileService API**: Leverages the native Android `TileService` to provide system-level UI components.
- **BOLT Repository Integration**: Uses [StudentRepository.getLastActiveStudentId] for $O(1)$ identification of the "Last Active Student," enabling rapid, context-aware logging.
- **Reactive Preferences**: [GhostHudTileService] directly toggles experimental flags in the [GhostPreferencesStore], reflecting changes instantly in the Seating Chart UI.

## 🔦 The Discovery
- **"Eyes-Free" Feedback**: We discovered that providing a "Logged!" visual confirmation on the tile itself is sufficient feedback for teachers, reducing the need to enter the main app.
- **Contextual Intelligence**: By automatically targeting the *last active* student, we eliminate the friction of student selection in rapid-feedback scenarios.

## 💡 The "What if?"
*What if the Quick Log tile could use device proximity (NFC or Bluetooth LE) to automatically target the student the teacher is physically standing next to?*

---
*Ghost - Rapid Prototyping for the Classroom of 2027*
