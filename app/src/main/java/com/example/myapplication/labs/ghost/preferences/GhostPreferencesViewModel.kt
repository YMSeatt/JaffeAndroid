package com.example.myapplication.labs.ghost.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GhostPreferencesViewModel: Reactive interface for the experimental Ghost preference store.
 *
 * Exposes experimental UI flags (Glow, Haptics, Glassmorphism) as lifecycle-aware [StateFlow]s
 * to facilitate smooth real-time UI updates during R&D.
 */
@HiltViewModel
class GhostPreferencesViewModel @Inject constructor(
    private val store: GhostPreferencesStore
) : ViewModel() {

    val glowIntensity: StateFlow<Float> = store.ghostGlowIntensity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val neuralHapticsEnabled: StateFlow<Boolean> = store.neuralHapticEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val glassmorphismEnabled: StateFlow<Boolean> = store.glassmorphismEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val scanlineEffectEnabled: StateFlow<Boolean> = store.scanlineEffectEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lodEnabled: StateFlow<Boolean> = store.lodEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setGlowIntensity(intensity: Float) {
        viewModelScope.launch {
            store.updateGlowIntensity(intensity)
        }
    }

    fun setNeuralHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.updateNeuralHapticEnabled(enabled)
        }
    }

    fun setGlassmorphismEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.updateGlassmorphismEnabled(enabled)
        }
    }

    fun setScanlineEffectEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.updateScanlineEffectEnabled(enabled)
        }
    }

    fun setLodEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.updateLodEnabled(enabled)
        }
    }
}
