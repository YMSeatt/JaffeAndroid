package com.example.myapplication.labs.ghost.hub

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.labs.ghost.util.GhostHapticManager
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import kotlin.math.*

/**
 * GhostHubLayer: A futuristic radial quick-action menu for Ghost Lab.
 *
 * This component provides a centralized interaction point for activating high-frequency
 * Ghost experiments. It uses an AGSL-backed radial background for visual feedback.
 *
 * ### Architectural Intent:
 * The Hub is designed to be a "Zero-Friction" interface. By centering the menu on the user's
 * long-press position, it minimizes travel time and leverages spatial memory for selection.
 *
 * @param isVisible Controls the visibility and entry/exit animations of the hub.
 * @param position The center point (in absolute pixels) where the hub was triggered.
 * @param onActionSelected Callback triggered when the user releases their touch over a segment.
 * @param onDismiss Callback triggered when the hub should be closed without an action.
 */
@Composable
fun GhostHubLayer(
    isVisible: Boolean,
    position: Offset,
    onActionSelected: (GhostAction) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val hapticManager = remember { GhostHapticManager(context) }

    val actions = remember {
        listOf(
            GhostAction("HUD", Icons.Default.AutoFixHigh, "Tactical HUD"),
            GhostAction("VISION", Icons.Default.PhotoCamera, "Ghost Vision"),
            GhostAction("PHANTASM", Icons.Default.BlurOn, "Neural Presence"),
            GhostAction("SPECTRA", Icons.Default.Palette, "Data Refraction"),
            GhostAction("AURORA", Icons.Default.Waves, "Climate Visualization"),
            GhostAction("FUTURE", Icons.Default.Update, "Neural Future"),
            GhostAction("STRATEGIST", Icons.Default.Psychology, "Neural Strategist"),
            GhostAction("SYNC", Icons.Default.Link, "Neural Sync"),
            GhostAction("LASSO", Icons.Default.Gesture, "Neural Lasso"),
            GhostAction("PIP", Icons.Default.PictureInPicture, "Neural PiP")
        )
    }

    var selectedAngle by remember { mutableFloatStateOf(0f) }
    var highlightAlpha by remember { mutableFloatStateOf(0f) }
    var activeActionIndex by remember { mutableIntStateOf(-1) }

    val transition = updateTransition(targetState = isVisible, label = "hub_transition")
    val hubScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "scale"
    ) { if (it) 1f else 0f }

    val hubAlpha by transition.animateFloat(
        transitionSpec = { tween(300) },
        label = "alpha"
    ) { if (it) 1f else 0f }

    if (hubScale <= 0f && !isVisible) return

    val infiniteTransition = rememberInfiniteTransition(label = "hub_pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isVisible) {
                if (!isVisible) return@pointerInput
                detectHubGestures(
                    center = position,
                    onMove = { angle ->
                        selectedAngle = angle
                        highlightAlpha = 1f

                        val segmentSize = 2 * PI.toFloat() / actions.size
                        var normalizedAngle = angle + PI.toFloat() // 0 to 2PI
                        // Align with action indices (starting from right/0 rad)
                        // atan2 returns -PI to PI. Actions are placed around the circle.

                        val idx = (((angle + PI.toFloat() + (segmentSize / 2f)) % (2 * PI.toFloat())) / segmentSize).toInt().coerceIn(0, actions.size - 1)
                        if (idx != activeActionIndex) {
                            activeActionIndex = idx
                            hapticManager.perform(GhostHapticManager.Pattern.UI_CLICK)
                        }
                    },
                    onRelease = {
                        if (activeActionIndex != -1) {
                            onActionSelected(actions[activeActionIndex])
                            hapticManager.perform(GhostHapticManager.Pattern.SUCCESS)
                        }
                        onDismiss()
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(position.x.toInt() - 150.dp.roundToPx(), position.y.toInt() - 150.dp.roundToPx()) }
                .size(300.dp)
                .scale(hubScale)
                .alpha(hubAlpha)
        ) {
            // Shader Background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val shader = remember { RuntimeShader(GhostHubShader.RADIAL_HUB) }
                val brush = remember(shader) { ShaderBrush(shader) }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iSelectedAngle", selectedAngle)
                    shader.setFloatUniform("iHighlightAlpha", highlightAlpha)
                    shader.setFloatUniform("iSegmentCount", actions.size.toFloat())
                    drawRect(brush = brush)
                }
            }

            // Icons
            actions.forEachIndexed { index, action ->
                val angle = (index * (2 * PI / actions.size)).toFloat() - PI.toFloat()
                val radius = 100.dp

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset {
                        // Position icons around the circle using polar coordinates.
                            IntOffset(
                                (cos(angle) * radius.toPx()).toInt(),
                                (sin(angle) * radius.toPx()).toInt()
                            )
                        }
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = index == activeActionIndex
                    val iconScale by animateFloatAsState(if (isSelected) 1.4f else 1.0f, label = "icon_scale")

                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) Color.Cyan.copy(alpha = 0.2f) else Color.Transparent,
                        modifier = Modifier.fillMaxSize().scale(iconScale)
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            tint = if (isSelected) Color.Cyan else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // Center Logo
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Ghost Hub",
                tint = Color.Cyan,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
            )
        }
    }
}

data class GhostAction(
    val id: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Custom gesture detector for the radial hub.
 *
 * Uses a low-level [awaitEachGesture] loop to track the user's touch movement relative to the
 * hub center. This allows for precise, low-latency selection tracking that is independent of
 * standard Compose button or click behaviors.
 *
 * @param center The origin point of the hub.
 * @param onMove Callback providing the calculated angle (in radians) of the user's finger.
 * @param onRelease Callback triggered when the user lifts their finger.
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectHubGestures(
    center: Offset,
    onMove: (Float) -> Unit,
    onRelease: () -> Unit
) {
    androidx.compose.foundation.gestures.awaitEachGesture {
        // Wait for the initial touch down
        val down = awaitFirstDown()

        while (true) {
            val event = awaitPointerEvent()
            val dragEvent = event.changes.firstOrNull() ?: break

            if (dragEvent.pressed) {
                val pos = dragEvent.position
                // Calculate the displacement vector from the center
                val dx = pos.x - center.x
                val dy = pos.y - center.y

                // Convert Cartesian coordinates to Polar angle (-PI to PI)
                val angle = atan2(dy, dx)
                onMove(angle)
            } else {
                // User released contact; finalize the selection
                onRelease()
                break
            }
        }
    }
}
