package com.example.myapplication.labs.ghost.supernova

import com.example.myapplication.data.BehaviorEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

/**
 * GhostSupernovaEngine: Manages the lifecycle of a "Classroom Supernova".
 *
 * This engine tracks the "Core Pressure" of the classroom atmosphere. When high-intensity
 * behavioral activity (especially negative friction) reaches critical mass,
 * it triggers a Supernova event—a visual and logical "reset" of the classroom energy.
 *
 * ### Architectural Role:
 * The Supernova serves as a high-fidelity feedback mechanism for the teacher. By visualizing
 * cumulative stress, it allows for proactive intervention before classroom stability
 * is compromised.
 *
 * ### Lifecycle Stages:
 * 1. **[SupernovaStage.IDLE]**: Baseline state where core pressure accumulates based on logs.
 * 2. **[SupernovaStage.CONTRACTION]**: The seating chart visually implodes as pressure peaks.
 * 3. **[SupernovaStage.EXPLOSION]**: A rapid AGSL pulse "clears" the visual field.
 * 4. **[SupernovaStage.NEBULA]**: A slow, cooling background effect signals stability.
 */
class GhostSupernovaEngine {

    /**
     * Defines the sequential phases of a Supernova event.
     */
    enum class SupernovaStage {
        /** Monitoring behavioral logs and calculating Core Pressure. */
        IDLE,
        /** 1.5s implementation phase: UI implodes toward the center. */
        CONTRACTION,
        /** 1.0s explosion phase: High-energy shockwave clears the UI. */
        EXPLOSION,
        /** 5.0s cooling phase: Procedural nebula returns the UI to a stable state. */
        NEBULA
    }

    private val _stage = MutableStateFlow(SupernovaStage.IDLE)
    /** The current lifecycle stage of the Supernova. */
    val stage = _stage.asStateFlow()

    private val _pressure = MutableStateFlow(0f)
    /**
     * The current normalized Core Pressure (0.0 to 1.0).
     * Triggers [SupernovaStage.CONTRACTION] when it reaches 1.0.
     */
    val pressure = _pressure.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    /** The animation progress (0.0 to 1.0) of the current [stage]. */
    val progress = _progress.asStateFlow()

    /**
     * Updates the core pressure based on behavioral logs within a sliding window.
     *
     * ### Balancing Logic (Parity with Python Suite):
     * Classroom stability is modeled as a thermodynamic system.
     * - **Negative logs**: Weighted at **0.1f** each. These represent high-friction events
     *   that rapidly increase system temperature and pressure.
     * - **Positive logs**: Weighted at **0.04f** each. These represent baseline energy
     *   that contributes to the overall "heat" but at a lower rate of instability.
     * - **Temporal Decay**: Uses a **15-minute window** (900,000ms). Logs older than this
     *   are discarded, allowing the classroom to naturally "cool down" during quiet periods.
     *
     * @param behaviorLog Complete list of behavioral events from the database.
     */
    fun updatePressure(behaviorLogs: List<BehaviorEvent>) {
        if (_stage.value != SupernovaStage.IDLE) return

        // BOLT: Filter logs within the 15-minute sliding window.
        val recentLogs = behaviorLogs.filter { System.currentTimeMillis() - it.timestamp < 15 * 60 * 1000 }
        if (recentLogs.isEmpty()) {
            _pressure.value = 0f
            return
        }

        var totalPressure = 0f
        recentLogs.forEach { log ->
            // Heuristic: Negative behavior has 2.5x the "pressure impact" of positive behavior.
            totalPressure += if (log.type.contains("Negative", ignoreCase = true)) 0.1f else 0.04f
        }

        _pressure.value = totalPressure.coerceIn(0f, 1.0f)

        // The Event Horizon: Trigger the contraction when critical mass is reached.
        if (_pressure.value >= 1.0f) {
            triggerSupernova()
        }
    }

    /**
     * Manually triggers a Supernova event, bypassing the pressure accumulation phase.
     */
    fun triggerSupernova() {
        if (_stage.value != SupernovaStage.IDLE) return
        _stage.value = SupernovaStage.CONTRACTION
    }

    /**
     * Updates the animation progress for the current stage.
     * Called by the [GhostSupernovaLayer] during animation frames.
     */
    fun updateProgress(value: Float) {
        _progress.value = value
    }

    /**
     * Transitions the engine to the next sequential stage in the lifecycle.
     */
    fun nextStage() {
        _stage.value = when (_stage.value) {
            SupernovaStage.IDLE -> SupernovaStage.CONTRACTION
            SupernovaStage.CONTRACTION -> SupernovaStage.EXPLOSION
            SupernovaStage.EXPLOSION -> SupernovaStage.NEBULA
            SupernovaStage.NEBULA -> SupernovaStage.IDLE
        }
        _progress.value = 0f
    }

    /**
     * Resets the engine to the IDLE state and clears all pressure metrics.
     */
    fun reset() {
        _stage.value = SupernovaStage.IDLE
        _pressure.value = 0f
        _progress.value = 0f
    }

    companion object {
        /**
         * Calculates the "Supernova Criticality" index for a classroom.
         *
         * This heuristic evaluates the global risk state by combining log density
         * with the ratio of negative friction. Maintains parity with the
         * `Python/ghost_supernova_analysis.py` R&D suite.
         *
         * @param studentCount Number of students in the classroom.
         * @param logCount Total number of logs recorded in the current session.
         * @param negativeRatio Ratio of negative logs to total logs (0.0 to 1.0).
         * @return Normalized criticality index (0.0 to 1.0).
         */
        fun calculateCriticality(studentCount: Int, logCount: Int, negativeRatio: Float): Float {
            // The 5.0 divisor represents the "Satiation Point"—the average logs per
            // student before the classroom is considered saturated with data.
            val base = logCount.toFloat() / (max(1, studentCount) * 5.0f)

            // The stress multiplier disproportionately weights negative logs (up to 3x).
            val stressMultiplier = 1.0f + (negativeRatio * 2.0f)

            return (base * stressMultiplier).coerceIn(0f, 1f)
        }
    }
}
