package com.example.myapplication.labs.ghost.weather

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.abs
import kotlin.random.Random

/**
 * GhostWeatherEngine: Manages the "Neural Weather" atmospheric simulation.
 *
 * This engine simulates dynamic weather patterns (Rain, Snow, Wind, Lightning)
 * driven by classroom behavioral and academic data.
 *
 * ### BOLT ⚡ Optimization:
 * 1. **Zero-Allocation Physics**: Uses primitive arrays for particle coordinates and states.
 * 2. **Particle Pooling**: Fixed pool of 200 particles to minimize GC pressure.
 * 3. **Efficient Data Mapping**: Single-pass analysis of recent logs.
 */
class GhostWeatherEngine {
    companion object {
        const val MAX_PARTICLES = 200
        const val CANVAS_SIZE = 4000f
        const val SPAWN_WINDOW_MS = 600_000L // 10 minutes
        const val GRAVITY_RAIN = 15f
        const val GRAVITY_SNOW = 3f
    }

    enum class WeatherMode { RAIN, SNOW, NEURAL_STORM }

    // Particle State Buffers
    val partX = FloatArray(MAX_PARTICLES)
    val partY = FloatArray(MAX_PARTICLES)
    val partVelX = FloatArray(MAX_PARTICLES)
    val partVelY = FloatArray(MAX_PARTICLES)
    val partActive = BooleanArray(MAX_PARTICLES)
    val partType = IntArray(MAX_PARTICLES) // 0: Rain, 1: Snow

    // Lightning State
    var lightningAlpha = 0f
    var lightningX = 0f
    var lastLightningTime = 0L

    // Global Weather State
    var currentMode = WeatherMode.RAIN
    var windForce = 0f // -100 to 100
    var intensity = 0.1f // 0 to 1

    private val random = Random(System.currentTimeMillis())
    private var lastUpdate = System.currentTimeMillis()

    /**
     * Synthesizes climate state from classroom data.
     * BOLT: Separated from physics loop to allow background processing and lazy evaluation.
     */
    fun updateClimate(
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>
    ) {
        val now = System.currentTimeMillis()

        // 1. Analyze Behavior Climate (Single-pass traversal)
        var recentCount = 0
        var positiveCount = 0
        var negativeCount = 0

        for (i in behaviorLogs.indices) {
            val log = behaviorLogs[i]
            if (now - log.timestamp < SPAWN_WINDOW_MS) {
                recentCount++
                if (log.type.contains("Negative", ignoreCase = true)) {
                    negativeCount++
                } else {
                    positiveCount++
                }
            } else if (log.timestamp < now - SPAWN_WINDOW_MS) {
                // BOLT: Break early as logs are sorted DESC by timestamp
                break
            }
        }

        // Intensity driven by log density
        intensity = (recentCount.toFloat() / 20f).coerceIn(0.05f, 1.0f)

        // Wind driven by behavioral balance (Positive -> East, Negative -> West)
        windForce = (positiveCount - negativeCount) * 10f

        // Mode driven by "Social Temperature"
        currentMode = when {
            negativeCount > positiveCount * 2 && recentCount > 5 -> WeatherMode.NEURAL_STORM
            positiveCount > recentCount * 0.8 && recentCount > 5 -> WeatherMode.SNOW
            else -> WeatherMode.RAIN
        }

        // 2. Lightning Trigger (Triggered by high-impact academic events)
        var hasRecentQuiz = false
        for (i in quizLogs.indices) {
            if (now - quizLogs[i].timestamp < 60_000L) {
                hasRecentQuiz = true
                break
            } else if (quizLogs[i].timestamp < now - 60_000L) {
                break
            }
        }

        if (hasRecentQuiz && now - lastLightningTime > 5000) {
            lightningAlpha = 1.0f
            lightningX = random.nextFloat() * CANVAS_SIZE
            lastLightningTime = now
        }
    }

    /**
     * Updates weather physics independent of data analysis.
     * BOLT: Optimized for 60fps execution on Dispatchers.Default.
     */
    fun updatePhysics(dt: Float) {
        // 1. Lightning Decay
        if (lightningAlpha > 0f) {
            lightningAlpha -= dt * 3f
        }

        // 2. Particle Physics
        val gravity = if (currentMode == WeatherMode.SNOW) GRAVITY_SNOW else GRAVITY_RAIN
        val spawnProbability = intensity * 2f

        for (i in 0 until MAX_PARTICLES) {
            if (partActive[i]) {
                // Apply Wind and Gravity
                partX[i] += (partVelX[i] + windForce) * dt
                partY[i] += partVelY[i] * dt

                if (currentMode == WeatherMode.SNOW) {
                    partVelY[i] = gravity + (random.nextFloat() * 2f)
                    partX[i] += kotlin.math.sin(System.currentTimeMillis().toFloat() / 500f + i) * 2f
                } else {
                    partVelY[i] += gravity * dt * 50f
                }

                // Bounds Check
                if (partY[i] > CANVAS_SIZE || partX[i] < -200f || partX[i] > CANVAS_SIZE + 200f) {
                    partActive[i] = false
                }
            } else if (random.nextFloat() < spawnProbability * dt * 10f) {
                // Spawn new particle
                partActive[i] = true
                partType[i] = if (currentMode == WeatherMode.SNOW) 1 else 0
                partX[i] = random.nextFloat() * CANVAS_SIZE
                partY[i] = -50f
                partVelX[i] = 0f
                partVelY[i] = if (currentMode == WeatherMode.SNOW) gravity else 100f
            }
        }
    }
}
