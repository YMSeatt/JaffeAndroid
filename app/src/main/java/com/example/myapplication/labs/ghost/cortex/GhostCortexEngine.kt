package com.example.myapplication.labs.ghost.cortex

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import kotlin.math.sqrt

/**
 * GhostCortexEngine: Neural Intent & Somatic Exploration Engine.
 *
 * This engine bridges the gap between abstract classroom data and physical sensation.
 * It maps student "Neural Tension" to complex haptic feedback patterns, allowing
 * teachers to "feel" the classroom's data landscape through the device's actuators.
 *
 * ### 2027 R&D Directive:
 * Leverages high-fidelity [VibrationEffect.Composition] primitives for nuanced
 * tactile communication of student behavioral and academic states.
 */
class GhostCortexEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Calculates the "Neural Tension" for a student using an RMS-based intensity model.
     * Formula: Tension = sqrt((academic_entropy^2 + behavioral_turbulence^2) / 2)
     *
     * ### Component Weights:
     * - **Academic Entropy**: Derived from quiz scores. A missing history defaults to 0.8 (Stable).
     *   Higher variance or lower scores increase entropy.
     * - **Behavioral Turbulence**: Calculated as the ratio of negative events to total events.
     *
     * @param quizLogs Historical academic data for score mapping.
     * @param behaviorLogs Historical behavioral events for turbulence mapping.
     * @return Normalized tension from 0.0 (Zen) to 1.0 (Critical).
     */
    fun calculateNeuralTension(
        quizLogs: List<QuizLog>,
        behaviorLogs: List<BehaviorEvent>
    ): Float {
        // Academic Entropy: High variance or low scores increase entropy.
        // We use 0.8f as a baseline "stable" score for students with no logs.
        val scores = quizLogs.mapNotNull { it.markValue?.toFloat()?.div(it.maxMarkValue?.toFloat() ?: 1f) }
        val avgScore = if (scores.isEmpty()) 0.8f else scores.average().toFloat()
        val academicEntropy = (1.0f - avgScore).coerceIn(0f, 1f)

        // Behavioral Turbulence: Ratio of negative logs to total logs.
        val turbulence = if (behaviorLogs.isEmpty()) 0.0f else {
            behaviorLogs.count { it.type.contains("Negative", ignoreCase = true) }.toFloat() / behaviorLogs.size
        }

        // RMS normalization ensures high intensity in either pillar drives the overall tension.
        return sqrt((academicEntropy * academicEntropy + turbulence * turbulence) / 2f).coerceIn(0f, 1f)
    }

    /**
     * Triggers a "Somatic Pulse" based on neural tension.
     * Maps the 0.0-1.0 tension range to four distinct haptic composition levels.
     *
     * ### Haptic Mapping:
     * - **Level 1 (< 0.2)**: Subtle Low Tick (30% amplitude).
     * - **Level 2 (< 0.5)**: Double Click (50% and 30% amplitude).
     * - **Level 3 (< 0.8)**: Urgent Tick (80%) + Spin (50%, API 31+).
     * - **Level 4 (>= 0.8)**: Critical Thud (100%) + Quick Fall (100%, API 31+).
     *
     * @param tension Normalized neural tension (0.0 to 1.0).
     */
    fun triggerSomaticPulse(tension: Float) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val composition = VibrationEffect.startComposition()

            when {
                tension < 0.2f -> {
                    // Level 1: Subtle, calm tick
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.3f)
                }
                tension < 0.5f -> {
                    // Level 2: Noticeable clicks
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.5f)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.3f, 50)
                }
                tension < 0.8f -> {
                    // Level 3: Urgent ticks and spin
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.5f, 20)
                    }
                }
                else -> {
                    // Level 4: Critical thuds and quick fall
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 1.0f, 30)
                    }
                }
            }

            vibrator.vibrate(composition.compose())
        } else {
            // Fallback for older hardware
            @Suppress("DEPRECATION")
            vibrator.vibrate((tension * 100).toLong().coerceAtLeast(10))
        }
    }

    /**
     * Logic Parity Note:
     * Aligned with theoretical `Python/ghost_cortex_analysis.py` for mapping
     * data clusters to tactile intensity vectors.
     */
    companion object {
        const val VERSION = "1.0.0-GHOST"
    }
}
