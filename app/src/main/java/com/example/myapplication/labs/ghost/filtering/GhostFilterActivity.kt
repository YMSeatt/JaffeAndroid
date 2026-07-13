package com.example.myapplication.labs.ghost.filtering

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * GhostFilterActivity: Standalone activity for testing the student filter experiment.
 *
 * This activity acts as the host for [GhostFilterScreen], allowing for rapid
 * R&D iteration on UI filtering performance and animations.
 */
@AndroidEntryPoint
class GhostFilterActivity : ComponentActivity() {

    private val viewModel: GhostFilterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // HARDEN: Prevent screenshots and screen recordings of sensitive filtered classroom data
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            MyApplicationTheme {
                GhostFilterScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
