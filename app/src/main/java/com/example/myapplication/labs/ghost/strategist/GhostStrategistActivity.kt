package com.example.myapplication.labs.ghost.strategist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.labs.ghost.GhostOracle
import kotlinx.coroutines.launch

/**
 * GhostStrategistActivity: A standalone sandbox for testing the AI Tactical Co-Pilot.
 *
 * This activity facilitates R&D for the [GhostStrategistEngine] by providing a
 * controlled environment where developers can trigger AI synthesis using mocked
 * classroom datasets. It allows for the validation of:
 * 1. **Heuristic Mapping**: How raw data translates into wordy tactics.
 * 2. **Haptic Feedback**: Testing the Android 15/16 vibration compositions.
 * 3. **Shader Integration**: Verifying the Neural Stream visualization during synthesis.
 */
class GhostStrategistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                GhostStrategistScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostStrategistScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var interventions by remember { mutableStateOf<List<GhostStrategistEngine.TacticalIntervention>>(emptyList()) }
    var isThinking by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf(GhostStrategistEngine.StrategistGoal.STABILITY) }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ghost Strategist 👻") },
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.Cyan,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Generative Tactical Advisor",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text("Select Strategic Objective:", color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GhostStrategistEngine.StrategistGoal.values().forEach { goal ->
                        FilterChip(
                            selected = selectedGoal == goal,
                            onClick = { selectedGoal = goal },
                            label = { Text(goal.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = Color.Gray,
                                selectedLabelColor = Color.Cyan,
                                selectedContainerColor = Color.Cyan.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isThinking = true
                            interventions = emptyList()
                            // Mocking data for the demo
                            interventions = GhostStrategistEngine.generateInterventions(
                                students = emptyList(),
                                behaviorLogs = listOf(
                                    BehaviorEvent(1L, "Negative behavior", System.currentTimeMillis() - 1000, null),
                                    BehaviorEvent(1L, "Negative behavior", System.currentTimeMillis() - 2000, null)
                                ),
                                quizLogs = emptyList(),
                                prophecies = listOf(
                                    GhostOracle.Prophecy(2, GhostOracle.ProphecyType.SOCIAL_FRICTION, "Friction", 0.95f),
                                    GhostOracle.Prophecy(3, GhostOracle.ProphecyType.ENGAGEMENT_DROP, "Drop", 0.75f)
                                ),
                                goal = selectedGoal
                            )
                            isThinking = false
                        }
                    },
                    enabled = !isThinking,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    if (isThinking) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Cyan)
                    } else {
                        Text("Synthesize Battle Plan")
                    }
                }
            }

            // Ghost Strategist visualization layer
            GhostStrategistLayer(
                interventions = interventions,
                isActive = true,
                isThinking = isThinking
            )
        }
    }
}
