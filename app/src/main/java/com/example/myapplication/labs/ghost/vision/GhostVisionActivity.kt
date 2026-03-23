package com.example.myapplication.labs.ghost.vision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.SeatingChartViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * GhostVisionActivity: Neural AR Calibration Sandbox.
 *
 * This activity provides a dedicated, full-screen environment for the Ghost Vision AR
 * experiment. It's designed to showcase the "High Wow Factor" of sensor-driven
 * student data projection.
 */
@AndroidEntryPoint
class GhostVisionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val viewModel: SeatingChartViewModel = viewModel()
                val students by viewModel.studentsForDisplay.collectAsState(initial = emptyList())
                val engine = remember { GhostVisionEngine(this) }

                DisposableEffect(Unit) {
                    engine.start()
                    onDispose { engine.stop() }
                }

                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("Ghost Vision AR", color = Color.Cyan) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Cyan)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .padding(padding)
                    ) {
                        // The AR Layer
                        GhostVisionLayer(
                            engine = engine,
                            students = students,
                            isActive = true
                        )
                    }
                }
            }
        }
    }
}
