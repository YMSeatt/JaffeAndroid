package com.example.myapplication.labs.ghost.pip

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.labs.ghost.GhostAuroraLayer
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.SeatingChartViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * GhostPipActivity: Native Picture-in-Picture Classroom Climate Monitor.
 *
 * This activity provides a minimal, persistent view of the classroom's "Neural Climate"
 * (driven by the Aurora shader) that can be used while multi-tasking.
 */
@AndroidEntryPoint
class GhostPipActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // HARDEN: Prevent screenshots and screen recordings of sensitive student data
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            MyApplicationTheme {
                val viewModel: SeatingChartViewModel = viewModel()
                val behaviorLogs by viewModel.allBehaviorEvents.observeAsState(emptyList())
                val quizLogs by viewModel.allQuizLogs.observeAsState(emptyList())
                val homeworkLogs by viewModel.allHomeworkLogs.observeAsState(emptyList())

                val isPipMode by isPipModeState.collectAsState()

                GhostPipContent(
                    behaviorLogs = behaviorLogs,
                    quizLogs = quizLogs,
                    homeworkLogs = homeworkLogs,
                    isPipMode = isPipMode,
                    onEnterPip = { enterPipMode() }
                )
            }
        }
    }

    private val isPipModeState = MutableStateFlow(false)

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipModeState.value = isInPictureInPictureMode
    }
}

@Composable
fun GhostPipContent(
    behaviorLogs: List<com.example.myapplication.data.BehaviorEvent>,
    quizLogs: List<com.example.myapplication.data.QuizLog>,
    homeworkLogs: List<com.example.myapplication.data.HomeworkLog>,
    isPipMode: Boolean,
    onEnterPip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background Aurora
        GhostAuroraLayer(
            behaviorLogs = behaviorLogs,
            quizLogs = quizLogs,
            homeworkLogs = homeworkLogs
        )

        // Overlay Info
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isPipMode) 8.dp else 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NEURAL CLIMATE",
                color = Color.Cyan,
                fontSize = if (isPipMode) 12.sp else 24.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            if (!isPipMode) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onEnterPip,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.2f))
                ) {
                    Text("ENTER PiP MONITOR", color = Color.Cyan)
                }
            }
        }
    }
}
