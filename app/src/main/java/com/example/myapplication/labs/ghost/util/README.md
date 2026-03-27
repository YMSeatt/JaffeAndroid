# Ghost Haptics Library 👻

This directory contains the `GhostHapticManager`, a high-fidelity haptic feedback wrapper designed for experimental "Ghost Lab" features.

## Why Ghost Haptics?

Standard Android `HapticFeedbackConstants` are often too generic for specialized UI experiments. `GhostHapticManager` leverages **Android 15 Primitives** and **API 31+ Vibrator Compositions** to provide "eyes-free" tactical awareness, allowing users to feel the state of the system (e.g., AI "thinking" or a "spark" of energy) without looking at the screen.

## Patterns

| Pattern | Metaphor | Native API Implementation |
| :--- | :--- | :--- |
| `SUCCESS` | Double-check confirmation. | `PRIMITIVE_TICK` (x2) on API 31+ |
| `ERROR` | A physical "thud" or obstruction. | Waveform with varying amplitudes. |
| `NEURAL_THINKING` | Rotational momentum/buildup. | `PRIMITIVE_SPIN` sequence on API 31+ |
| `UI_CLICK` | Clean, sharp interaction. | `EFFECT_CLICK` on API 29+ |
| `SPARK_POP` | Sudden, light burst of energy. | `PRIMITIVE_QUICK_RISE` + `PRIMITIVE_TICK` |

## Usage

```kotlin
val hapticManager = remember { GhostHapticManager(context) }

// Trigger a pattern
hapticManager.perform(GhostHapticManager.Pattern.SUCCESS)
```

## Hardware Support & Fallbacks

- **API 35 (Android 15):** Full support for advanced primitives.
- **API 31+:** Uses `VibrationEffect.Composition` for multi-stage patterns.
- **API 26-30:** Falls back to `VibrationEffect` one-shots and waveforms.
- **Pre-API 26:** Falls back to legacy `vibrator.vibrate(milliseconds)`.
