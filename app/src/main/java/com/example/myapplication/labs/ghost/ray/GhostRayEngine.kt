package com.example.myapplication.labs.ghost.ray

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * GhostRayEngine: Manages the spatial orientation and intersection logic for the Ghost Ray.
 *
 * It utilizes the device's rotation vector sensor to track orientation in 3D space,
 * projecting the device's "forward" vector onto the 2D seating chart canvas.
 * It also triggers haptic feedback when the ray intersects with student nodes.
 */
class GhostRayEngine(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _rayTarget = MutableStateFlow<Offset?>(null)
    /** The projected screen coordinate where the device is currently pointing. */
    val rayTarget = _rayTarget.asStateFlow()

    private val _intersectedStudentId = MutableStateFlow<Long?>(null)
    /** The ID of the student currently highlighted by the ray. */
    val intersectedStudentId = _intersectedStudentId.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var lastIntersectedId: Long? = null

    /**
     * Starts tracking device orientation.
     */
    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Stops tracking device orientation.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
        _rayTarget.value = null
        _intersectedStudentId.value = null
        lastIntersectedId = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // azimuth = orientation[0], pitch = orientation[1], roll = orientation[2]
            // We use pitch and roll to map to a 2D plane.
            // This is a simplified projection for the PoC.

            // COORDINATE MAPPING:
            // The mapping factor (2000f) and offset (500f) translate device orientation
            // into the 4000x4000 logical canvas space.
            // Roll (orientation[2]) maps to X, and negative Pitch (-orientation[1]) maps to Y.
            val x = orientation[2] * 2000f + 500f
            val y = -orientation[1] * 2000f + 500f

            _rayTarget.value = Offset(x, y)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Updates the intersection state based on current ray target and student positions.
     *
     * This method performs a spatial query to find which student (if any) is currently
     * intersected by the projected ray.
     *
     * ### Spatial Normalization:
     * Student positions are stored in logical units, so they are first scaled and
     * offset to match the current screen/pixel space of the [Canvas] before
     * intersection testing.
     *
     * @param students List of students currently on the canvas.
     * @param canvasScale The current zoom level of the canvas.
     * @param canvasOffset The current pan offset of the canvas.
     */
    fun updateIntersection(
        students: List<StudentUiItem>,
        canvasScale: Float,
        canvasOffset: Offset
    ) {
        val target = _rayTarget.value ?: return
        var foundId: Long? = null

        // BOLT: Use squared distance to avoid expensive sqrt() calls in the intersection loop.
        // A 60f radius (scaled) is used as the hit box for student icons.
        val threshold = 60f * canvasScale
        val thresholdSq = threshold * threshold

        for (student in students) {
            val screenX = student.xPosition.value * canvasScale + canvasOffset.x
            val screenY = student.yPosition.value * canvasScale + canvasOffset.y

            val dx = screenX - target.x
            val dy = screenY - target.y
            val distSq = dx * dx + dy * dy

            if (distSq < thresholdSq) {
                foundId = student.id.toLong()
                break
            }
        }

        if (foundId != lastIntersectedId) {
            if (foundId != null) {
                triggerIntersectionHaptic()
            }
            lastIntersectedId = foundId
            _intersectedStudentId.value = foundId
        }
    }

    private fun triggerIntersectionHaptic() {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val composition = VibrationEffect.startComposition()
            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.6f)
            vibrator.vibrate(composition.compose())
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }
}
