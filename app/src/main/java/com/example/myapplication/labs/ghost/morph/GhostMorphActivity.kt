package com.example.myapplication.labs.ghost.morph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background

/**
 * GhostMorphActivity: Demonstrates high-fidelity Shared Bounds transitions.
 *
 * This activity implements the "Ghost Morph" effect by orchestrating a transition
 * between a "Placeholder" state (representing the student icon on the seating chart)
 * and the "Dossier" state (full screen report).
 */
@AndroidEntryPoint
class GhostMorphActivity : ComponentActivity() {

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val studentId = intent.getLongExtra("STUDENT_ID", -1L)
        val studentName = intent.getStringExtra("STUDENT_NAME") ?: "Unknown Student"

        setContent {
            MyApplicationTheme {
                var isDossierVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    // Delay slightly to ensure layout is ready for transition
                    kotlinx.coroutines.delay(100)
                    isDossierVisible = true
                }

                SharedTransitionLayout {
                    AnimatedContent(
                        targetState = isDossierVisible,
                        transitionSpec = {
                            fadeIn(tween(600)) togetherWith fadeOut(tween(600))
                        },
                        label = "morph_transition"
                    ) { targetState ->
                        if (!targetState) {
                            // Initial State: Represents the student icon arriving from Seating Chart
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .sharedBounds(
                                            rememberSharedContentState(key = "student_bounds"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                            resizeMode = SharedTransitionScope.ResizeMode.Scale
                                        )
                                        .clip(CircleShape),
                                    color = Color.Cyan
                                ) {}
                            }
                        } else {
                            // Final State: The full Neural Dossier
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .sharedBounds(
                                        rememberSharedContentState(key = "student_bounds"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        resizeMode = SharedTransitionScope.ResizeMode.Scale
                                    )
                            ) {
                                GhostDossierScreen(
                                    studentId = studentId,
                                    studentName = studentName,
                                    onDismiss = {
                                        isDossierVisible = false
                                        finish()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
