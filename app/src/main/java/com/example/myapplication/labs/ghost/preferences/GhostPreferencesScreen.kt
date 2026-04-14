package com.example.myapplication.labs.ghost.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.labs.ghost.util.GhostGlassmorphicSurface

/**
 * GhostPreferencesScreen: The R&D interface for experimental UI customization.
 *
 * Implements a futuristic dark-mode UI with "Ghost Lab" branding.
 * Uses Material 3 components to toggle experimental DataStore-backed flags.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostPreferencesScreen(
    viewModel: GhostPreferencesViewModel,
    onBack: () -> Unit
) {
    val glowIntensity by viewModel.glowIntensity.collectAsState()
    val neuralHapticsEnabled by viewModel.neuralHapticsEnabled.collectAsState()
    val glassmorphismEnabled by viewModel.glassmorphismEnabled.collectAsState()
    val scanlineEffectEnabled by viewModel.scanlineEffectEnabled.collectAsState()
    val lodEnabled by viewModel.lodEnabled.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "GHOST LAB PREFERENCES",
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            GhostSectionTitle("VISUAL ENGINE")

            GhostGlassmorphicSurface(
                modifier = Modifier.fillMaxWidth(),
                glassmorphismEnabled = glassmorphismEnabled
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GhostPreferenceSlider(
                        label = "Glow Intensity",
                        value = glowIntensity,
                        onValueChange = viewModel::setGlowIntensity
                    )

                    GhostPreferenceSwitch(
                        label = "Glassmorphism Effect",
                        description = "Enable experimental blurred translucent UI layers.",
                        checked = glassmorphismEnabled,
                        onCheckedChange = viewModel::setGlassmorphismEnabled
                    )

                    GhostPreferenceSwitch(
                        label = "Scanline Overlay",
                        description = "Apply a retro-futuristic CRT scanline filter.",
                        checked = scanlineEffectEnabled,
                        onCheckedChange = viewModel::setScanlineEffectEnabled
                    )

                    GhostPreferenceSwitch(
                        label = "Adaptive LOD",
                        description = "Dynamically adjust UI detail based on zoom level.",
                        checked = lodEnabled,
                        onCheckedChange = viewModel::setLodEnabled
                    )
                }
            }

            GhostSectionTitle("CHROMA ENGINE")

            GhostGlassmorphicSurface(
                modifier = Modifier.fillMaxWidth(),
                glassmorphismEnabled = glassmorphismEnabled
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GhostPreferenceSwitch(
                        label = "Dynamic Color",
                        description = "Use Material You dynamic color extraction (API 31+).",
                        checked = dynamicColorEnabled,
                        onCheckedChange = viewModel::setDynamicColorEnabled
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Theme Mode", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("LIGHT", "DARK", "GHOST").forEach { mode ->
                                val isSelected = themeMode == mode
                                GhostSegmentedButton(
                                    label = mode,
                                    isSelected = isSelected,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            GhostSectionTitle("SOMATIC SYSTEMS")

            GhostGlassmorphicSurface(
                modifier = Modifier.fillMaxWidth(),
                glassmorphismEnabled = glassmorphismEnabled
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GhostPreferenceSwitch(
                        label = "Neural Haptics",
                        description = "Use high-fidelity Android 15 haptic primitives for feedback.",
                        checked = neuralHapticsEnabled,
                        onCheckedChange = viewModel::setNeuralHapticsEnabled
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                "EXPERIMENTAL R&D BUILD v2027.04",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
            )
        }
    }
}

@Composable
fun GhostSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            color = Color.Cyan,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Black
        )
    )
}

@Composable
fun GhostPreferenceSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurface)
            Text("${(value * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color.Cyan,
                activeTrackColor = Color.Cyan,
                inactiveTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun GhostSegmentedButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GhostPreferenceSwitch(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Cyan,
                checkedTrackColor = Color.Cyan.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
