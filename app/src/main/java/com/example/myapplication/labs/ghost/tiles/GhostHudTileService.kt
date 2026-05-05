package com.example.myapplication.labs.ghost.tiles

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.myapplication.labs.ghost.preferences.GhostPreferencesStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * GhostHudTileService: A Quick Settings Tile to toggle the Tactical HUD.
 *
 * This service allows teachers to enable/disable the Ghost HUD (Tactical Radar)
 * directly from the Android Quick Settings panel, providing rapid, eyes-free control
 * over experimental visualizers.
 *
 * Integrated with [GhostPreferencesStore] for persistent state management.
 */
@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class GhostHudTileService : TileService() {

    @Inject
    lateinit var ghostPreferencesStore: GhostPreferencesStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        job?.cancel()
        job = serviceScope.launch {
            ghostPreferencesStore.scanlineEffectEnabled.collect { isActive ->
                updateTileState(isActive)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        job?.cancel()
        job = null
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            // HUD state is currently modeled via scanline effect in preferences for this PoC
            // or we could add a dedicated HUD toggle to preferences.
            // For now, we'll use the 'scanlineEffectEnabled' as a proxy for HUD visibility
            // in this experiment.
            val current = ghostPreferencesStore.scanlineEffectEnabled.first()
            ghostPreferencesStore.updateScanlineEffectEnabled(!current)
        }
    }

    private fun updateTileState(isActive: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Ghost HUD"
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
