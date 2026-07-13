# 👻 Ghost Lab: The Logic Parity Bridge

This document explains the architectural relationship between the **Python R&D Suite** and the **Android Production App**. Understanding this "Bridge" is essential for maintainers looking to port new experimental features or update existing ones.

## 🏛️ The R&D Workflow

Experimental features follow a strict "Python-First" development lifecycle:
1.  **Modeling**: A new pedagogical or spatial theory is modeled in a standalone Python script (e.g., `ghost_tectonics_analysis.py`).
2.  **Verification**: The script is run against real classroom JSON exports to verify the heuristics and generate sample reports.
3.  **Porting**: The validated logic is ported to a Kotlin "Engine" in the Android app (e.g., `GhostTectonicEngine.kt`).
4.  **Hardware Synthesis**: The logic is integrated with Android-specific APIs (AGSL Shaders, Haptics, Sensors).

## 📐 Coordinate & Mathematical Parity

To ensure that a layout optimized in Python looks identical on Android, the system maintains strict mathematical parity:

| Metric | Python R&D | Android Production | Scaling Factor |
| :--- | :--- | :--- | :--- |
| **Canvas Width** | 2000 units | 4000 units | 2x |
| **Canvas Height** | 1500 units | 4000 units | N/A (Logical Fill) |
| **Collab Radius** | 1000 units | 2000 units | 2x |
| **Friction Radius**| 1500 units | 3000 units | 2x |
| **Blueprint Frame**| 1200x800 SVG | 1200x800 SVG | 1:1 |

### 🔄 Coordinate Translation Rules
When mapping between platforms, the following rules apply:
- **Python to Android**: Standard ingestion maps coordinates 1:1. Since Android has a larger canvas, Python layouts appear in the top-left quadrant.
- **Android to Blueprint**: The formula `(coordinate / 4) + offset` is used to project the 4000x4000 production canvas into the 1200x800 SVG frame.

## ⛓️ Shared JSON Schema (v10)

Both platforms communicate using a unified, versioned JSON schema. The `ClassroomDataDto` (Android) and the Python `json` structures are logically identical, containing:

-   **Students**: ID (UUID string/Long), Name, Gender, Group ID, Position (x, y).
-   **Furniture**: ID, Name, Type, Dimensions, Position.
-   **Logs**: Unified event stream for Behavior, Quizzes, and Homework.

## 🛰️ Logic Gold-Standards

The Python scripts in this directory serve as the **Gold Standard** for the Android implementation. If a discrepancy is found between the two, the Python script's output is generally considered the "Mathematically Correct" baseline, as it is easier to verify using standard data analysis tools.

---
*Documentation love letter from Scribe 📜*
