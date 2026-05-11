package com.example.myapplication.labs.ghost.palette

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.labs.ghost.util.GhostChromaTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * GhostPaletteActivity: The interactive sandbox for the Ghost Palette experiment.
 *
 * This activity enables teachers to customize the classroom's "Neural Theme"
 * using a gesture-driven color harmony engine.
 */
@AndroidEntryPoint
class GhostPaletteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // HARDEN: Protect sensitive experimental customization from unauthorized capture
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            GhostChromaTheme {
                GhostPaletteScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}
