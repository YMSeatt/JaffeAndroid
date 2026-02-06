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
 *
 * This class leverages Android's [SpeechRecognizer] to provide a hands-free experience for
 * teachers. It parses spoken language into specific classroom actions like logging behavior,
 * starting/ending sessions, or triggering layout optimizations.
 *
 * @param context Android context for initializing the speech engine.
 * @param viewModel The shared ViewModel used to execute commands.
 * @param onAmplitudeChange Callback for real-time volume updates (used for visualization).
 * @param onListeningStateChange Callback to notify UI when the microphone is active.
 * @param onResult Callback to provide the recognized text back to the UI.
 * @param customBehaviors Optional list of user-defined behavior names to improve parsing accuracy.
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

    /**
     * Parses the recognized text into structured commands.
     *
     * Supported phrases:
     * - "Undo" / "Redo": Reverses or reapplies the last action.
     * - "Start Session" / "Begin Session": Triggers the live class session.
     * - "End Session" / "Stop Session": Finalizes the current session.
     * - "Log [behavior] for [student]": Logs a specific behavior event.
     * - "Optimize" / "Rearrange": Triggers the [GhostCognitiveEngine] layout optimization.
     *
     * @param text The raw string recognized by the speech engine.
     */
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

    /**
     * Specifically handles behavioral logging via voice.
     *
     * It attempts to extract:
     * 1. **Behavior Type**: Matches against [customBehaviors] or falls back to keywords (positive, negative, question).
     * 2. **Student Identity**: Scans the command for student full names or nicknames.
     *
     * @param command The recognized command string.
     */
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
            command.contains(student.fullName.value.lowercase()) ||
            (student.nickname.value?.lowercase()?.let { command.contains(it) } ?: false)
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
            Log.d("GhostVoice", "Successfully logged $behaviorType for student with initials ${student.initials.value}")
        } ?: Log.w("GhostVoice", "Could not identify student in log command.")
    }
}
