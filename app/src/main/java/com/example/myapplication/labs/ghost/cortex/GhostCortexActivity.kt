package com.example.myapplication.labs.ghost.cortex

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * GhostCortexActivity: A Proof of Concept for Predictive Back in 2027.
 *
 * This activity demonstrates the "Neural History" view, showcasing how teachers
 * can peer into future data trajectories and use the modern Predictive Back gesture
 * to fluidly return to the seating chart.
 *
 * ### R&D Directive:
 * - High-fidelity somatic transitions.
 * - Integration with `OnBackInvokedCallback` (via Compose BackHandler).
 */
class GhostCortexActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // HARDEN: Prevent screenshots and screen recordings of sensitive neural history data
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            MyApplicationTheme {
                GhostCortexScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostCortexScreen(onBack: () -> Unit) {
    var tension by remember { mutableFloatStateOf(0.7f) }
    val context = LocalContext.current
    val engine = remember { GhostCortexEngine(context) }

    // BOLT: Enable Predictive Back support via Compose BackHandler
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neural History 👻") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.Cyan
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.Cyan,
                    modifier = Modifier.size(120.dp).graphicsLayer {
                        rotationZ = tension * 360f
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Somatic Data Trajectory",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Predicting behavioral shifts for Q4 2027...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(48.dp))

                Slider(
                    value = tension,
                    onValueChange = {
                        tension = it
                        engine.triggerSomaticPulse(it)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Cyan,
                        activeTrackColor = Color.Cyan
                    )
                )

                Text(
                    "Neural Tension: ${(tension * 100).toInt()}%",
                    color = Color.Cyan
                )

                Spacer(modifier = Modifier.height(64.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Return to Reality")
                }
            }

            // Somatic Shader Layer
            GhostCortexLayer(
                engine = engine,
                isActive = true,
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.4f }
            )

            // Somatic Glow Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = tension * 0.3f
                    }
                    .background(Color.Magenta.copy(alpha = 0.1f))
            )
        }
    }
}
