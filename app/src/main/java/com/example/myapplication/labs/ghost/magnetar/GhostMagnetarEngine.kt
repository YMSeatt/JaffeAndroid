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
     * Calculates the magnetic dipoles for each student based on their behavioral history.
     */
    fun calculateDipoles(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>
    ): List<MagneticDipole> {
        val groupedLogs = behaviorLogs.groupBy { it.studentId }

        return students.map { student ->
            val sid = student.id.toLong()
            val logs = groupedLogs[sid] ?: emptyList()

            // Calculate polarity based on log counts
            var northWeight = 0f
            var southWeight = 0f

            logs.forEach { log ->
                if (log.type.contains("Positive", ignoreCase = true)) {
                    northWeight += 1.0f
                } else if (log.type.contains("Negative", ignoreCase = true)) {
                    southWeight += 1.5f // Negative behaviors exert stronger "Social Gravity"
                }
            }

            val strength = (northWeight - southWeight).coerceIn(-10f, 10f)
            val radius = (abs(strength) * 50f + 100f).coerceIn(100f, 600f)

            MagneticDipole(
                studentId = sid,
                x = student.xPosition.value,
                y = student.yPosition.value,
                strength = strength,
                radius = radius
            )
        }
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

        val quadrantIntensities = quadrants.map { (name, pos) ->
            var fx = 0.0
            var fy = 0.0
            val (qx, qy) = pos

            dipoles.forEach { d ->
                val dx = (qx - d.x).toDouble()
                val dy = (qy - d.y).toDouble()
                val r2 = dx * dx + dy * dy + 100.0
                val r3 = Math.pow(r2, 1.5)

                // Field contribution (Python parity: * 5000.0)
                fx += (dx / r3) * d.strength.toDouble() * 5000.0
                fy += (dy / r3) * d.strength.toDouble() * 5000.0
            }

            val totalStrength = Math.sqrt(fx * fx + fy * fy).toFloat()
            FieldStrength(name, totalStrength)
        }

        val avgIntensity = if (quadrantIntensities.isNotEmpty()) {
            quadrantIntensities.map { it.intensity }.average().toFloat()
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
        analysis.quadrantIntensities.forEach { q ->
            report.append("- ${q.quadrantName}: ${String.format(java.util.Locale.US, "%.4f", q.intensity)} μG\n")
        }

        report.append("\n## [MAGNETIC POLARITY MAP]\n")
        analysis.dipoles.forEach { d ->
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
