package com.example.myapplication.labs.ghost

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max

/**
 * GhostEchoEngine: Monitors classroom acoustic energy.
 *
 * This engine uses [AudioRecord] to sample microphone input and calculate
 * the current amplitude, which is then normalized and exposed as a flow.
 * It is used by the Ghost Echo UI to drive ambient "acoustic turbulence" visualizations.
 */
class GhostEchoEngine {
    private val _amplitude = MutableStateFlow(0f)

    /**
     * Normalized amplitude (0.0 to 1.0).
     */
    val amplitude = _amplitude.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val sampleRate = 8000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(1024)

    /**
     * Starts monitoring acoustic energy.
     * Requires RECORD_AUDIO permission to be granted externally.
     */
    @SuppressLint("MissingPermission")
    fun start() {
        if (recordingJob != null) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return
            }

            audioRecord?.startRecording()

            recordingJob = scope.launch {
                val buffer = ShortArray(bufferSize)
                while (isActive) {
                    val record = audioRecord ?: break
                    if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) break

                    val readCount = record.read(buffer, 0, bufferSize)
                    if (readCount > 0) {
                        var maxAmp = 0f
                        for (i in 0 until readCount) {
                            maxAmp = max(maxAmp, abs(buffer[i].toInt()).toFloat())
                        }

                        // Normalize 0..32767 to 0..1.
                        // We use a lower ceiling (16384) for sensitivity in typical classroom noise.
                        val normalized = (maxAmp / 16384f).coerceIn(0f, 1f)

                        // Apply smoothing to prevent jittery visualizations
                        _amplitude.value = _amplitude.value * 0.7f + normalized * 0.3f
                    }
                    delay(50)
                }
            }
        } catch (e: Exception) {
            // Silently fail if hardware or permissions are unavailable
            stop()
        }
    }

    /**
     * Stops monitoring and releases hardware resources.
     */
    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        audioRecord = null
        _amplitude.value = 0f
    }
}
