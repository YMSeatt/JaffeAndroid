package com.example.myapplication.labs.ghost.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.ghostDataStore: DataStore<Preferences> by preferencesDataStore(name = "ghost_settings")

/**
 * GhostPreferencesStore: A modern, DataStore-backed repository for experimental UI preferences.
 *
 * This store manages "Ghost Lab" specific settings that aren't yet ready for the primary
 * production [AppPreferencesRepository]. It allows for rapid R&D iteration on UI polish
 * and experimental features like AGSL glow intensities and custom haptics.
 */
@Singleton
class GhostPreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val GHOST_GLOW_INTENSITY = floatPreferencesKey("ghost_glow_intensity")
        val NEURAL_HAPTIC_ENABLED = booleanPreferencesKey("neural_haptic_enabled")
        val GLASSMORPHISM_ENABLED = booleanPreferencesKey("glassmorphism_enabled")
        val SCANLINE_EFFECT_ENABLED = booleanPreferencesKey("scanline_effect_enabled")
        val LOD_ENABLED = booleanPreferencesKey("lod_enabled")
        val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        val GHOST_THEME_MODE = stringPreferencesKey("ghost_theme_mode")
    }

    /** Flow of the experimental glow intensity for AGSL shaders. */
    val ghostGlowIntensity: Flow<Float> = context.ghostDataStore.data.map { prefs ->
        prefs[Keys.GHOST_GLOW_INTENSITY] ?: 0.5f
    }

    /** Flow indicating if advanced Android 15 neural haptics are enabled. */
    val neuralHapticEnabled: Flow<Boolean> = context.ghostDataStore.data.map { prefs ->
        prefs[Keys.NEURAL_HAPTIC_ENABLED] ?: true
    }

    /** Flow indicating if the experimental glassmorphism UI layer is active. */
    val glassmorphismEnabled: Flow<Boolean> = context.ghostDataStore.data.map { prefs ->
        prefs[Keys.GLASSMORPHISM_ENABLED] ?: false
    }

    /** Flow indicating if the CRT-style scanline overlay is active. */
    val scanlineEffectEnabled: Flow<Boolean> = context.ghostDataStore.data.map { prefs ->
        prefs[Keys.SCANLINE_EFFECT_ENABLED] ?: false
    }

    /** Flow indicating if the adaptive Level of Detail engine is active. */
    val lodEnabled: Flow<Boolean> = context.ghostDataStore.data.map { prefs ->
        prefs[Keys.LOD_ENABLED] ?: true
    }

    /** Flow indicating if Material You dynamic color is enabled in Ghost Lab. */
    val dynamicColorEnabled: Flow<Boolean> = context.ghostDataStore.data.map { prefs ->
        prefs[Keys.DYNAMIC_COLOR_ENABLED] ?: true
    }

    /** Flow of the current Ghost theme mode (LIGHT, DARK, GHOST). */
    val ghostThemeMode: Flow<String> = context.ghostDataStore.data.map { prefs ->
        prefs[Keys.GHOST_THEME_MODE] ?: "GHOST"
    }

    suspend fun updateGlowIntensity(intensity: Float) {
        context.ghostDataStore.edit { it[Keys.GHOST_GLOW_INTENSITY] = intensity }
    }

    suspend fun updateNeuralHapticEnabled(enabled: Boolean) {
        context.ghostDataStore.edit { it[Keys.NEURAL_HAPTIC_ENABLED] = enabled }
    }

    suspend fun updateGlassmorphismEnabled(enabled: Boolean) {
        context.ghostDataStore.edit { it[Keys.GLASSMORPHISM_ENABLED] = enabled }
    }

    suspend fun updateScanlineEffectEnabled(enabled: Boolean) {
        context.ghostDataStore.edit { it[Keys.SCANLINE_EFFECT_ENABLED] = enabled }
    }

    suspend fun updateLodEnabled(enabled: Boolean) {
        context.ghostDataStore.edit { it[Keys.LOD_ENABLED] = enabled }
    }

    suspend fun updateDynamicColorEnabled(enabled: Boolean) {
        context.ghostDataStore.edit { it[Keys.DYNAMIC_COLOR_ENABLED] = enabled }
    }

    suspend fun updateGhostThemeMode(mode: String) {
        context.ghostDataStore.edit { it[Keys.GHOST_THEME_MODE] = mode }
    }
}
