package com.example.myapplication.labs.ghost.preferences

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.labs.ghost.util.GhostChromaTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * GhostPreferencesActivity: Sandbox entry point for experimental UI settings.
 *
 * This activity acts as a standalone Proof of Concept (PoC) for the DataStore-backed
 * preference engine, allowing for isolated R&D testing.
 */
@AndroidEntryPoint
class GhostPreferencesActivity : ComponentActivity() {

    private val viewModel: GhostPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColorEnabled.collectAsState()

            GhostChromaTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {
                GhostPreferencesScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
