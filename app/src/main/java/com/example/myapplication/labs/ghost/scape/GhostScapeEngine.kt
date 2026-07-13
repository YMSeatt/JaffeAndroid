package com.example.myapplication.labs.ghost.scape

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.abs

/**
 * GhostScapeEngine: Neural Spatial Audio monitoring engine.
 *
 * This engine generates procedural "Neural Pings" using [AudioTrack].
 * Pings are spatially panned across the stereo field based on student positions
 * on the 4000x4000 logical canvas.
 *
 * Frequency and intensity are driven by student behavioral "Agitation" (negative counts).
 */
class GhostScapeEngine {
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var audioTrack: AudioTrack? = null

    private val _pingEvents = MutableSharedFlow<PingEvent>(extraBufferCapacity = 10)
    val pingEvents = _pingEvents.asSharedFlow()

    data class PingEvent(
        val studentId: Long,
        val x: Float,
        val y: Float,
        val frequency: Float,
        val intensity: Float
    )

    /**
     * Plays a neural ping for a specific student.
     *
     * BOLT: Offloaded to Dispatchers.Default to avoid blocking the UI thread during
     * procedural sample generation and AudioTrack.write (blocking MODE_STREAM).
     */
    suspend fun playPing(studentId: Long, logicalX: Float, logicalY: Float, frequency: Float, intensity: Float) = withContext(Dispatchers.Default) {
        if (audioTrack == null) {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        }

        // Calculate stereo pan: -1.0 (Left) to 1.0 (Right)
        // Logical X is 0 to 4000.
        val pan = ((logicalX / 4000f) * 2f - 1f).coerceIn(-1f, 1f)

        // Emit event for UI ripple synchronization
        _pingEvents.tryEmit(PingEvent(studentId, logicalX, logicalY, frequency, intensity))

        // Generate short sine wave with envelope
        val durationMs = 150
        val numSamplesTotal = (sampleRate * durationMs / 1000) * 2
        val samples = ShortArray(numSamplesTotal)

        // Simple linear panning
        val lVol = (1.0f - pan).coerceIn(0f, 1f) * intensity * 0.5f
        val rVol = (1.0f + pan).coerceIn(0f, 1f) * intensity * 0.5f

        val numFrames = numSamplesTotal / 2
        for (i in 0 until numFrames) {
            val t = i.toDouble() / sampleRate
            val envelope = if (i < numFrames / 8) {
                i.toFloat() / (numFrames / 8f) // Attack
            } else {
                (1f - (i - numFrames / 8f) / (numFrames * 7f / 8f)).coerceAtLeast(0f) // Decay
            }

            val value = (sin(2.0 * PI * frequency * t) * 32767.0 * envelope).toInt().toShort()
            samples[i * 2] = (value * lVol).toInt().toShort()
            samples[i * 2 + 1] = (value * rVol).toInt().toShort()
        }

        audioTrack?.write(samples, 0, numSamplesTotal)
    }

    fun release() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore release errors
        }
        audioTrack = null
    }
}
