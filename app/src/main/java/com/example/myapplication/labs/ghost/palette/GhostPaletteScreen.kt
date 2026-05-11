package com.example.myapplication.labs.ghost.palette

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.labs.ghost.preferences.GhostPreferencesViewModel
import com.example.myapplication.labs.ghost.util.GhostGlassmorphicSurface
import com.example.myapplication.labs.ghost.util.GhostHapticManager

/**
 * GhostPaletteScreen: Gesture-driven color harmony engine UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostPaletteScreen(
    prefsViewModel: GhostPreferencesViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hapticManager = remember { GhostHapticManager(context) }

    var harmonyMode by remember { mutableStateOf("COMPLEMENTARY") }
    var touchPos by remember { mutableStateOf(Offset.Zero) }

    val currentPrimary by prefsViewModel.ghostPrimaryColor.collectAsState()
    val currentSecondary by prefsViewModel.ghostSecondaryColor.collectAsState()

    val initialHsv = remember(currentPrimary) {
        GhostPaletteEngine.rgbToHsv(Color(currentPrimary))
    }

    var h by remember { mutableFloatStateOf(initialHsv[0]) }
    var s by remember { mutableFloatStateOf(initialHsv[1]) }
    var v by remember { mutableFloatStateOf(0.8f) } // High value for vibrant R&D palette

    val (previewPrimary, previewSecondary) = remember(h, s, v, harmonyMode) {
        GhostPaletteEngine.calculateHarmony(h, s, v, harmonyMode)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "palette_pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "GHOST PALETTE",
                        style = LocalTextStyle.current.copy(
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.Cyan
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
        ) {
            // Neural Color Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            touchPos = change.position
                            h = (touchPos.x / size.width).coerceIn(0f, 1f) * 360f
                            s = (touchPos.y / size.height).coerceIn(0f, 1f)
                            hapticManager.perform(GhostHapticManager.Pattern.UI_CLICK)
                        }
                    }
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val shader = remember { RuntimeShader(GhostPaletteShader.NEURAL_PALETTE_FIELD) }
                    val brush = remember(shader) { ShaderBrush(shader) }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        shader.setFloatUniform("iResolution", size.width, size.height)
                        shader.setFloatUniform("iTime", time)
                        shader.setFloatUniform("iBaseHSV", h, s, v)
                        shader.setFloatUniform("iTouchPos", touchPos.x, touchPos.y)
                        shader.setFloatUniform("iHarmonyMode", if (harmonyMode == "COMPLEMENTARY") 0f else 1f)
                        drawRect(brush = brush)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(previewPrimary.copy(alpha = 0.3f)))
                }

                Text(
                    "DRAG TO EXPLORE HARMONIES",
                    modifier = Modifier.align(Alignment.Center).alpha(0.5f),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, letterSpacing = 2.sp)
                )
            }

            // Controls & Preview
            GhostGlassmorphicSurface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                glassmorphismEnabled = true
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Neural Harmony", fontWeight = FontWeight.Bold, color = Color.Cyan)
                            Text("Select mathematical color relationship", fontSize = 10.sp, color = Color.Gray)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("COMPLEMENTARY", "TRIADIC").forEach { mode ->
                                val isSelected = harmonyMode == mode
                                Button(
                                    onClick = {
                                        harmonyMode = mode
                                        hapticManager.perform(GhostHapticManager.Pattern.SUCCESS)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color.Cyan else Color.DarkGray,
                                        contentColor = if (isSelected) Color.Black else Color.Cyan
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                ) {
                                    Text(mode, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PalettePreviewCircle("PRIMARY", previewPrimary, Modifier.weight(1f))
                        PalettePreviewCircle("SECONDARY", previewSecondary, Modifier.weight(1f))

                        FloatingActionButton(
                            onClick = {
                                prefsViewModel.setGhostPrimaryColor(previewPrimary.toArgb().toLong())
                                prefsViewModel.setGhostSecondaryColor(previewSecondary.toArgb().toLong())
                                hapticManager.perform(GhostHapticManager.Pattern.SUCCESS)
                                onBack()
                            },
                            containerColor = Color.Cyan,
                            contentColor = Color.Black,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Apply")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PalettePreviewCircle(label: String, color: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}
