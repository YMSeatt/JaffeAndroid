package com.example.myapplication.labs.ghost.ray

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.model.StudentUiItem
import androidx.compose.runtime.mutableStateListOf

/**
 * GhostRayActivity: A standalone R&D Sandbox for testing the Ghost Ray pointer.
 *
 * This activity provides an isolated environment to calibrate the ray and
 * verify the sensor-to-canvas mapping without dependencies on the main seating chart.
 */
class GhostRayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                GhostRayScreen(onBack = { finish() })
            }
        }
    }
}

private fun createMockStudent(id: Int, first: String, last: String, x: Float, y: Float): StudentUiItem {
    return StudentUiItem(
        id = id,
        fullName = mutableStateOf("$first $last"),
        nickname = mutableStateOf(null),
        initials = mutableStateOf(first.take(1) + last.take(1)),
        xPosition = mutableStateOf(x),
        yPosition = mutableStateOf(y),
        displayWidth = mutableStateOf(100.dp),
        displayHeight = mutableStateOf(100.dp),
        displayBackgroundColor = mutableStateOf(listOf(Color.Gray)),
        displayOutlineColor = mutableStateOf(listOf(Color.White)),
        displayTextColor = mutableStateOf(Color.White),
        displayOutlineThickness = mutableStateOf(2.dp),
        displayCornerRadius = mutableStateOf(8.dp),
        displayPadding = mutableStateOf(4.dp),
        fontFamily = mutableStateOf("monospace"),
        fontSize = mutableStateOf(12),
        fontColor = mutableStateOf(Color.White),
        recentBehaviorDescription = mutableStateOf(emptyList()),
        recentHomeworkDescription = mutableStateOf(emptyList()),
        recentQuizDescription = mutableStateOf(emptyList()),
        groupColor = mutableStateOf(null),
        groupId = mutableStateOf(null),
        sessionLogText = mutableStateOf(emptyList()),
        temporaryTask = mutableStateOf(null),
        irisParams = mutableStateOf(null),
        osmoticNode = mutableStateOf(null),
        altitude = mutableStateOf(0f),
        behaviorEntropy = mutableStateOf(0f),
        tectonicStress = mutableStateOf(0f),
        quasarEnergy = mutableStateOf(0f),
        quasarPolarity = mutableStateOf(0f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostRayScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val engine = remember { GhostRayEngine(context) }

    // Mock students for testing
    val students = remember {
        mutableStateListOf(
            createMockStudent(1, "Alice", "Test", 400f, 400f),
            createMockStudent(2, "Bob", "Beta", 1200f, 400f),
            createMockStudent(3, "Charlie", "Cloud", 800f, 1200f)
        )
    }

    val intersectedId by engine.intersectedStudentId.collectAsState()

    DisposableEffect(Unit) {
        engine.start()
        onDispose {
            engine.stop()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ghost Ray Calibration 👻") },
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
                Text(
                    "Tilt device to aim the Neural Beam.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (intersectedId != null) {
                    val student = students.find { it.id.toLong() == intersectedId }
                    Text(
                        "Intersected Node: ${student?.fullName?.value}",
                        color = Color.Magenta,
                        style = MaterialTheme.typography.headlineSmall
                    )
                } else {
                    Text(
                        "Searching for student nodes...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Return to Seating Chart")
                }
            }

            // Ghost Ray Layer
            GhostRayLayer(
                engine = engine,
                students = students,
                canvasScale = 1.0f,
                canvasOffset = Offset(0f, 0f),
                isActive = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
