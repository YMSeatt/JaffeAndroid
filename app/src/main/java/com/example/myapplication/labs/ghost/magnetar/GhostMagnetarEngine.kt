package com.example.myapplication.labs.ghost.magnetar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * GhostMagnetarEngine: Calculates classroom "Social Polarity" and "Magnetic Field Vectors".
 *
 * This engine maps behavioral logs to magnetic dipoles (Positive = North, Negative = South)
 * and integrates the device's physical Magnetometer to skew the field orientation.
 *
 * ### Field Model:
 * The engine implements an **Inverse-Square Magnetic Field** ($1/r^2$) where
 * students act as discrete dipoles.
 * - **North (+)**: Aligned with positive behavioral history.
 * - **South (-)**: Aligned with negative behavioral history.
 * - **Social Weight**: Negative behaviors exert **1.5x more magnetic pull**
 *   than positive behaviors, simulating their higher impact on social cohesion.
 */
class GhostMagnetarEngine(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _magneticHeading = MutableStateFlow(0f)
    val magneticHeading = _magneticHeading.asStateFlow()

    data class MagneticDipole(
        val studentId: Long,
        val x: Float,
        val y: Float,
        val strength: Float, // Positive = North (+), Negative = South (-)
        val radius: Float
    )

    data class FieldStrength(
        val quadrantName: String,
        val intensity: Float
    )

    data class MagnetarAnalysis(
        val globalStatus: String,
        val quadrantIntensities: List<FieldStrength>,
        val dipoles: List<MagneticDipole>
    )

    fun start() {
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            // Calculate a simple 2D heading from the magnetic field (X and Y components)
            val heading = Math.atan2(event.values[1].toDouble(), event.values[0].toDouble()).toFloat()
            _magneticHeading.value = heading
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * BOLT: Refactored to accept pre-calculated log counts, eliminating O(B) grouping and allocations.
     * Maps students to [MagneticDipole] instances using high-performance primitive calculations.
     */
    fun calculateDipoles(
        students: List<StudentUiItem>
    ): List<MagneticDipole> {
        val dipoles = ArrayList<MagneticDipole>(students.size)
        // BOLT: Use manual index loop to avoid iterator churn.
        val count = students.size
        for (i in 0 until count) {
            val student = students[i]
            dipoles.add(
                MagneticDipole(
                    studentId = student.id.toLong(),
                    x = student.xPosition.value,
                    y = student.yPosition.value,
                    strength = student.magneticStrength.value,
                    radius = student.magneticRadius.value
                )
            )
        }
        return dipoles
    }

    /**
     * Performs macroscopic field analysis based on student dipoles.
     * Ported from Python/ghost_magnetar_analysis.py.
     */
    fun analyzeMagneticField(dipoles: List<MagneticDipole>): MagnetarAnalysis {
        val quadrants = listOf(
            "Top-Left" to (1000f to 1000f),
            "Top-Right" to (3000f to 1000f),
            "Bottom-Left" to (1000f to 3000f),
            "Bottom-Right" to (3000f to 3000f)
        )

        val quadrantIntensities = ArrayList<FieldStrength>(quadrants.size)
        val dCount = dipoles.size
        var intensitySum = 0f

        // BOLT: Reverted to for-in loop for the 4-quadrant list for clarity,
        // while keeping the manual O(D) dipole loop for performance.
        for (quadrant in quadrants) {
            val (name, pos) = quadrant
            var fx = 0.0
            var fy = 0.0
            val qx = pos.first
            val qy = pos.second

            for (j in 0 until dCount) {
                val d = dipoles[j]
                val dx = (qx - d.x).toDouble()
                val dy = (qy - d.y).toDouble()
                val r2 = dx * dx + dy * dy + 100.0
                val r3 = Math.pow(r2, 1.5)

                // Field contribution (Python parity: * 5000.0)
                fx += (dx / r3) * d.strength.toDouble() * 5000.0
                fy += (dy / r3) * d.strength.toDouble() * 5000.0
            }

            val totalStrength = Math.sqrt(fx * fx + fy * fy).toFloat()
            quadrantIntensities.add(FieldStrength(name, totalStrength))
            intensitySum += totalStrength
        }

        // BOLT: Optimized avgIntensity calculation to avoid list allocations.
        val avgIntensity = if (quadrantIntensities.isNotEmpty()) {
            intensitySum / quadrantIntensities.size
        } else 0f

        val status = when {
            avgIntensity > 0.5f -> "SUPERCHARGED"
            avgIntensity > 0.1f -> "ACTIVE"
            else -> "STABLE"
        }

        return MagnetarAnalysis(status, quadrantIntensities, dipoles)
    }

    /**
     * Generates a Markdown report of the classroom's social magnetic field.
     * Parity-matched with the output of `Python/ghost_magnetar_analysis.py`.
     */
    fun generateMagnetarReport(
        analysis: MagnetarAnalysis,
        studentNames: Map<Long, String>
    ): String {
        val timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )

        val report = StringBuilder()
        report.append("# 👻 GHOST MAGNETAR: SOCIAL MAGNETIC ANALYSIS\n")
        report.append("**Classroom Field Status:** ${analysis.globalStatus}\n")
        report.append("**Analyzed Dipoles:** ${analysis.dipoles.size}\n")
        report.append("**Timestamp:** $timestamp\n\n")

        report.append("---\n\n")

        report.append("## [QUADRANT INTENSITY MAP]\n")
        val qIntensities = analysis.quadrantIntensities
        for (i in qIntensities.indices) {
            val q = qIntensities[i]
            report.append("- ${q.quadrantName}: ${String.format(java.util.Locale.US, "%.4f", q.intensity)} μG\n")
        }

        report.append("\n## [MAGNETIC POLARITY MAP]\n")
        val dipoles = analysis.dipoles
        for (i in dipoles.indices) {
            val d = dipoles[i]
            val name = studentNames[d.studentId] ?: "Student ${d.studentId}"
            val polarity = when {
                d.strength > 0 -> "NORTH (+)"
                d.strength < 0 -> "SOUTH (-)"
                else -> "NEUTRAL"
            }
            report.append("- [$name]: $polarity (Strength: ${String.format(java.util.Locale.US, "%.2f", d.strength)})\n")
        }

        report.append("\n---\n*Generated by Ghost Magnetar Analysis Bridge v1.0 (Experimental)*")

        return report.toString()
    }
}
