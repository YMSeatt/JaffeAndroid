package com.example.myapplication.labs.ghost

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GhostHorizonEngine: A context-aware atmospheric engine.
 *
 * It tracks ambient light and barometric pressure to drive a "Neural Horizon"
 * that adapts the UI to the physical classroom environment.
 */
class GhostHorizonEngine(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private val _lightLevel = MutableStateFlow(100f) // Lux
    private val _pressureLevel = MutableStateFlow(1013.25f) // hPa (Standard atmospheric pressure)

    val lightLevel = _lightLevel.asStateFlow()
    val pressureLevel = _pressureLevel.asStateFlow()

    fun start() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_LIGHT -> {
                // Low-pass filter to prevent flicker
                _lightLevel.value = _lightLevel.value * 0.9f + event.values[0] * 0.1f
            }
            Sensor.TYPE_PRESSURE -> {
                _pressureLevel.value = _pressureLevel.value * 0.95f + event.values[0] * 0.05f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Maps the light level to a spectrum interpolation factor.
     * 0.0 = Amber/Ghost (Dark), 1.0 = Solarized Cyan (Bright)
     */
    fun getAtmosphericFactor(): Float {
        return (lightLevel.value / 500f).coerceIn(0f, 1f)
    }

    /**
     * Maps pressure to a verticality offset.
     * Normalized relative to standard pressure.
     */
    fun getVerticality(): Float {
        return ((pressureLevel.value - 1000f) / 50f).coerceIn(-1f, 1f)
    }
}
