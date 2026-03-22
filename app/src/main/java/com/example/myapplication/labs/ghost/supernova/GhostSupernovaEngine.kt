package com.example.myapplication.labs.ghost.supernova

import com.example.myapplication.data.BehaviorEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

/**
 * GhostSupernovaEngine: Manages the lifecycle of a "Classroom Supernova".
 *
 * This engine tracks the "Core Pressure" of the classroom. When high-intensity
 * behavioral activity (especially negative friction) reaches critical mass,
 * it triggers a Supernova event—a visual and logical "reset" of the classroom energy.
 *
 * ### Lifecycle Stages:
 * 1. **IDLE**: Core pressure accumulates based on behavioral events.
 * 2. **CONTRACTION**: The seating chart visually shrinks as pressure peaks.
 * 3. **EXPLOSION**: A rapid AGSL pulse "clears" the visual field.
 * 4. **NEBULA**: A slow, cooling background effect signals a return to stability.
 */
class GhostSupernovaEngine {

    enum class SupernovaStage {
        IDLE,
        CONTRACTION,
        EXPLOSION,
        NEBULA
    }

    private val _stage = MutableStateFlow(SupernovaStage.IDLE)
    val stage = _stage.asStateFlow()

    private val _pressure = MutableStateFlow(0f)
    val pressure = _pressure.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    /**
     * Updates the core pressure based on behavioral logs.
     *
     * ### Balancing Logic:
     * Negative logs exert **2.5x more pressure** than positive logs (0.1f vs 0.04f),
     * reflecting the disproportionate impact of disruption on classroom stability.
     * Pressure decays naturally as logs fall out of the 15-minute analysis window.
     */
    fun updatePressure(behaviorLogs: List<BehaviorEvent>) {
        if (_stage.value != SupernovaStage.IDLE) return

        val recentLogs = behaviorLogs.filter { System.currentTimeMillis() - it.timestamp < 15 * 60 * 1000 }
        if (recentLogs.isEmpty()) {
            _pressure.value = 0f
            return
        }

        var totalPressure = 0f
        recentLogs.forEach { log ->
            totalPressure += if (log.type.contains("Negative", ignoreCase = true)) 0.1f else 0.04f
        }

        _pressure.value = totalPressure.coerceIn(0f, 1.0f)

        if (_pressure.value >= 1.0f) {
            triggerSupernova()
        }
    }

    fun triggerSupernova() {
        if (_stage.value != SupernovaStage.IDLE) return
        _stage.value = SupernovaStage.CONTRACTION
    }

    fun updateProgress(value: Float) {
        _progress.value = value
    }

    fun nextStage() {
        _stage.value = when (_stage.value) {
            SupernovaStage.IDLE -> SupernovaStage.CONTRACTION
            SupernovaStage.CONTRACTION -> SupernovaStage.EXPLOSION
            SupernovaStage.EXPLOSION -> SupernovaStage.NEBULA
            SupernovaStage.NEBULA -> SupernovaStage.IDLE
        }
        _progress.value = 0f
    }

    fun reset() {
        _stage.value = SupernovaStage.IDLE
        _pressure.value = 0f
        _progress.value = 0f
    }

    companion object {
        /**
         * Calculates the "Supernova Criticality" of a classroom.
         * Parity with Python/ghost_supernova_analysis.py.
         */
        fun calculateCriticality(studentCount: Int, logCount: Int, negativeRatio: Float): Float {
            val base = logCount.toFloat() / (max(1, studentCount) * 5.0f)
            val stressMultiplier = 1.0f + (negativeRatio * 2.0f)
            return (base * stressMultiplier).coerceIn(0f, 1f)
        }
    }
}
