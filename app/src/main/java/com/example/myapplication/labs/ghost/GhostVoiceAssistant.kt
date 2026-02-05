package com.example.myapplication.labs.ghost

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.viewmodel.SeatingChartViewModel
import java.util.*

/**
 * GhostVoiceAssistant: A Proof of Concept for Voice-Activated Classroom Management.
 * It uses Android's SpeechRecognizer to listen for commands and translate them into actions.
 */
class GhostVoiceAssistant(
    private val context: Context,
    private val viewModel: SeatingChartViewModel,
    private val onAmplitudeChange: (Float) -> Unit,
    private val onListeningStateChange: (Boolean) -> Unit,
    private val onResult: (String) -> Unit,
    private val customBehaviors: List<String> = emptyList()
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("GhostVoice", "Speech recognition not available")
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
            onListeningStateChange(true)
        } catch (e: Exception) {
            Log.e("GhostVoice", "Failed to start listening", e)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        onListeningStateChange(false)
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d("GhostVoice", "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.d("GhostVoice", "Beginning of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // rmsdB is typically -2 to 10+. Normalize to 0..1 for visualization.
        val normalized = ((rmsdB + 2) / 12f).coerceIn(0f, 1f)
        onAmplitudeChange(normalized)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        onListeningStateChange(false)
    }

    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
        Log.e("GhostVoice", "Error $error: $message")
        onListeningStateChange(false)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.firstOrNull()?.let { text ->
            parseAndExecuteCommand(text)
            onResult(text)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.firstOrNull()?.let { text ->
            onResult(text)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun parseAndExecuteCommand(text: String) {
        val command = text.lowercase(Locale.getDefault())
        Log.d("GhostVoice", "Parsing voice command (length: ${command.length})")

        when {
            command.contains("undo") -> viewModel.undo()
            command.contains("redo") -> viewModel.redo()
            command.contains("start session") || command.contains("begin session") -> viewModel.startSession()
            command.contains("end session") || command.contains("stop session") -> viewModel.endSession()
            command.contains("log") -> handleLogCommand(command)
            command.contains("optimize") || command.contains("rearrange") -> {
                if (GhostConfig.COGNITIVE_ENGINE_ENABLED) {
                    viewModel.runCognitiveOptimization()
                }
            }
        }
    }

    private fun handleLogCommand(command: String) {
        val students = viewModel.studentsForDisplay.value ?: emptyList()

        // Find behavior - match against custom behaviors or fall back to defaults
        var behaviorType = customBehaviors.find { it.lowercase() in command }

        if (behaviorType == null) {
            behaviorType = when {
                command.contains("positive") || command.contains("good") || command.contains("great") -> "Positive Participation"
                command.contains("negative") || command.contains("bad") -> "Negative behavior"
                command.contains("question") -> "Asked Question"
                else -> "Quick Note"
            }
        }

        // Find student name in command
        val targetStudent = students.find { student ->
            command.contains(student.fullName.lowercase()) ||
            (student.nickname?.lowercase()?.let { command.contains(it) } ?: false)
        }

        targetStudent?.let { student ->
            viewModel.addBehaviorEvent(
                BehaviorEvent(
                    studentId = student.id.toLong(),
                    type = behaviorType!!,
                    timestamp = System.currentTimeMillis(),
                    comment = null
                )
            )
            Log.d("GhostVoice", "Successfully logged $behaviorType for student with initials ${student.initials}")
        } ?: Log.w("GhostVoice", "Could not identify student in log command.")
    }
}
