package com.example.myapplication.labs.ghost.phasing

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf

/**
 * GhostPhasingEngine: Manages the lifecycle and physical feedback of the Phasing experiment.
 *
 * It tracks the transition state between the physical and neural layers and
 * triggers synchronized haptics during the "Phase Shift".
 */
class GhostPhasingEngine(private val context: Context) {

    private val _phaseLevel = mutableFloatStateOf(0f)
    val phaseLevel: State<Float> = _phaseLevel

    private val _isPhased = mutableStateOf(false)
    val isPhased: State<Boolean> = _isPhased

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Updates the current phase level and triggers haptics at key thresholds.
     */
    fun updatePhase(level: Float) {
        val oldLevel = _phaseLevel.floatValue
        _phaseLevel.floatValue = level.coerceIn(0f, 1f)
        _isPhased.value = _phaseLevel.floatValue > 0.5f

        // Trigger "Phase Shimmer" haptics
        if (oldLevel < 0.1f && level >= 0.1f) {
            triggerShimmerHaptic(0.3f)
        } else if (oldLevel < 0.9f && level >= 0.9f) {
            triggerShimmerHaptic(0.8f)
        }
    }

    private fun triggerShimmerHaptic(intensity: Float) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = if (intensity > 0.5f) {
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            } else {
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate((intensity * 20).toLong())
        }
    }

    /**
     * Executes a full "Phase Pulse" sequence.
     */
    fun triggerPulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val composition = VibrationEffect.startComposition()
            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.5f)
            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f, 50)
            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1.0f, 100)
            vibrator?.vibrate(composition.compose())
        }
    }
}
