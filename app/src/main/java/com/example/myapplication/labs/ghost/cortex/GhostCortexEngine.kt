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
     * Calculates the "Neural Tension" for a student.
     * Tension = sqrt(academic_entropy^2 + behavioral_turbulence^2)
     *
     * @param quizLogs Historical academic data.
     * @param behaviorLogs Historical behavioral data.
     * @return Normalized tension from 0.0 (Zen) to 1.0 (Critical).
     */
    fun calculateNeuralTension(
        quizLogs: List<QuizLog>,
        behaviorLogs: List<BehaviorEvent>
    ): Float {
        // Academic Entropy: High variance or low scores increase entropy
        val scores = quizLogs.mapNotNull { it.markValue?.toFloat()?.div(it.maxMarkValue?.toFloat() ?: 1f) }
        val avgScore = if (scores.isEmpty()) 0.8f else scores.average().toFloat()
        val academicEntropy = (1.0f - avgScore).coerceIn(0f, 1f)

        // Behavioral Turbulence: Ratio of negative logs to total logs
        val turbulence = if (behaviorLogs.isEmpty()) 0.0f else {
            behaviorLogs.count { it.type.contains("Negative", ignoreCase = true) }.toFloat() / behaviorLogs.size
        }

        return sqrt((academicEntropy * academicEntropy + turbulence * turbulence) / 2f).coerceIn(0f, 1f)
    }

    /**
     * Triggers a "Somatic Pulse" based on neural tension.
     * Higher tension results in more complex, sharp, and intense haptic feedback.
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
