package com.example.myapplication.labs.ghost.biometric

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GhostBiometricEngine: A Proof of Concept for native biometric authentication.
 *
 * This engine wraps the Android Biometric library to provide a unified interface
 * for fingerprint and face unlock capabilities. It is used to enhance the
 * "App Lock" security layer for the Seating Chart.
 *
 * ### Architectural Intent:
 * Leverages native security hardware to provide a "Zero-Touch" authentication flow
 * for teachers, replacing traditional password entry where supported.
 */
@Singleton
class GhostBiometricEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Checks if the device has compatible biometric hardware and if the user has
     * enrolled biometrics (Fingerprint, Face, etc.).
     */
    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Triggers the system biometric prompt.
     *
     * @param activity The host activity for the prompt.
     * @param title The title displayed in the prompt.
     * @param subtitle The subtitle displayed in the prompt.
     * @param onResult Callback for success or failure.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Neural Biometric Unlock",
        subtitle: String = "Authenticate to access classroom data",
        onResult: (Boolean) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(false)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(false)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use Password")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
