package com.example.myapplication.labs.ghost.rain

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.abs
import kotlin.random.Random

/**
 * GhostRainEngine: Manages the "Neural Rain" atmospheric simulation.
 *
 * This engine simulates "Data Droplets" falling across the 4000x4000 logical canvas.
 * The intensity of the rain is driven by recent classroom activity.
 *
 * ### BOLT ⚡ Optimization:
 * 1. **Zero-Allocation Physics**: Uses primitive arrays (`FloatArray`) for droplet
 *    coordinates, velocities, and splash states to eliminate GC pressure.
 * 2. **Droplet Pooling**: Maintains a fixed pool of 100 droplets, recycling them
 *    as they exit the frame or intersect with student icons.
 * 3. **Activity Scaling**: O(Recent) analysis of behavior logs to calculate spawn frequency.
 */
class GhostRainEngine {
    companion object {
        const val MAX_DROPLETS = 100
        const val CANVAS_SIZE = 4000f
        const val SPAWN_WINDOW_MS = 600_000L // 10 minutes
        const val GRAVITY = 15f
    }

    // Droplet State Buffers (Logical Coordinates)
    val dropX = FloatArray(MAX_DROPLETS)
    val dropY = FloatArray(MAX_DROPLETS)
    val dropVel = FloatArray(MAX_DROPLETS)
    val dropActive = BooleanArray(MAX_DROPLETS)
    val splashTime = FloatArray(MAX_DROPLETS) // 0.0 (no splash) to 1.0 (splash complete)

    private val random = Random(System.currentTimeMillis())
    private var lastUpdate = System.currentTimeMillis()

    /**
     * Updates droplet physics and handles intersections with students.
     *
     * @param students Current list of students for intersection checking.
     * @param recentLogs List of recent behavior logs to determine rain intensity.
     */
    fun update(
        students: List<StudentUiItem>,
        recentLogs: List<BehaviorEvent>
    ) {
        val now = System.currentTimeMillis()
        val dt = (now - lastUpdate) / 1000f
        lastUpdate = now

        // 1. Calculate Intensity (Spawn Rate)
        var activityCount = 0
        for (i in recentLogs.indices) {
            if (now - recentLogs[i].timestamp < SPAWN_WINDOW_MS) activityCount++
            else break // DESC sorted logs
        }

        // Base spawn rate + activity bonus
        val spawnProbability = (0.05f + (activityCount.toFloat() / 50f)).coerceAtMost(0.8f)

        // 2. Physics & Intersection Loop
        for (i in 0 until MAX_DROPLETS) {
            if (dropActive[i]) {
                if (splashTime[i] > 0f) {
                    // Splash Animation
                    splashTime[i] += dt * 2f
                    if (splashTime[i] >= 1.0f) {
                        resetDroplet(i)
                    }
                } else {
                    // Falling Physics
                    dropY[i] += dropVel[i] * dt * 100f
                    dropVel[i] += GRAVITY * dt

                    // Intersection Check (Student Icons)
                    for (j in students.indices) {
                        val s = students[j]
                        val sx = s.xPosition.value
                        val sy = s.yPosition.value
                        val sw = s.displayWidth.value.value * 20f // Rough Dp to logical conversion
                        val sh = s.displayHeight.value.value * 20f

                        if (dropX[i] >= sx && dropX[i] <= sx + sw &&
                            dropY[i] >= sy && dropY[i] <= sy + sh) {
                            splashTime[i] = 0.01f
                            break
                        }
                    }

                    // Bounds Check
                    if (dropY[i] > CANVAS_SIZE) {
                        resetDroplet(i)
                    }
                }
            } else if (random.nextFloat() < spawnProbability * dt * 10f) {
                // Spawn new droplet
                dropActive[i] = true
                dropX[i] = random.nextFloat() * CANVAS_SIZE
                dropY[i] = -100f
                dropVel[i] = 20f + random.nextFloat() * 30f
                splashTime[i] = 0f
            }
        }
    }

    private fun resetDroplet(index: Int) {
        dropActive[index] = false
        dropY[index] = -100f
        splashTime[index] = 0f
    }
}
