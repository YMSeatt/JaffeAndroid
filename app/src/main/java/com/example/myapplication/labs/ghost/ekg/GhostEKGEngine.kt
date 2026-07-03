package com.example.myapplication.labs.ghost.ekg

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student
import com.example.myapplication.labs.ghost.GhostBioSyncEngine
import kotlin.math.*

/**
 * GhostEKGEngine: Logic for synthesizing a "Neural EKG" signal for students.
 *
 * This engine generates a normalized waveform (0.0 to 1.0) that represents the student's
 * biological rhythm. The signal is modulated by:
 * 1. **Vitality**: Higher vitality results in a stronger, more defined amplitude.
 * 2. **Stress**: Higher stress increases the "heart rate" (frequency), simulating tachycardia.
 * 3. **Activity**: Recent interactions create momentary "spikes" in the waveform.
 *
 * BOLT Optimization: Uses a purely functional, zero-allocation synthesis loop for 60fps rendering.
 */
object GhostEKGEngine {

    /**
     * Synthesizes the EKG signal at a specific time point.
     *
     * @param time The elapsed time for the animation.
     * @param vitality Student's vitality (0.0 to 1.0).
     * @param stress Student's stress level (0.0 to 1.0).
     * @param recentSpike Intensity of a recent interaction spike (0.0 to 1.0).
     * @return The normalized signal value at this time point.
     */
    fun synthesizeSignal(
        time: Float,
        vitality: Float,
        stress: Float,
        recentSpike: Float
    ): Float {
        // Frequency modulation (Heart Rate)
        // Base HR is ~1Hz (normal). Stress can push it to ~3Hz.
        val baseFrequency = 1.0f + (stress * 2.0f)
        val phase = time * baseFrequency * PI.toFloat() * 2f

        // 1. The P-Wave (Atrial depolarization)
        val pWave = 0.1f * exp(-0.5f * (sin(phase - 0.5f) / 0.1f).pow(2))

        // 2. The QRS Complex (Ventricular depolarization) - The main spike
        val qrs = 1.0f * exp(-0.5f * (sin(phase) / 0.02f).pow(2))

        // 3. The T-Wave (Ventricular repolarization)
        val tWave = 0.2f * exp(-0.5f * (sin(phase + 0.8f) / 0.15f).pow(2))

        // Combine components and scale by vitality
        val baseSignal = (pWave + qrs + tWave) * vitality

        // Add the "Interaction Spike" (momental activity)
        val finalSignal = baseSignal + (recentSpike * 0.5f)

        return finalSignal.coerceIn(0.0f, 1.0f)
    }

    /**
     * Calculates the intensity of a "spike" based on very recent logs.
     */
    fun calculateRecentSpike(logs: List<BehaviorEvent>, now: Long): Float {
        if (logs.isEmpty()) return 0f
        val window = 5000L // 5 seconds
        val mostRecent = logs.maxOf { it.timestamp }
        val age = now - mostRecent
        if (age < 0 || age > window) return 0f

        // Linear decay of the spike
        return 1.0f - (age.toFloat() / window)
    }
}
