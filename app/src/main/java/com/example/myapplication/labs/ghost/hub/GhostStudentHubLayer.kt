package com.example.myapplication.labs.ghost.hub

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.labs.ghost.util.GhostHapticManager
import kotlin.math.*

/**
 * GhostStudentHubLayer: A radial quick-action menu specialized for student interactions.
 *
 * This component provides immediate access to pedagogical actions (Behavior, Academic, Insight)
 * directly from a student icon. It utilizes a violet-themed AGSL shader for a distinct
 * "Neural" aesthetic.
 *
 * @param isVisible Controls the visibility and entry/exit animations of the hub.
 * @param position The center point (in absolute pixels) where the hub was triggered.
 * @param onActionSelected Callback triggered when the user releases their touch over a segment.
 * @param onDismiss Callback triggered when the hub should be closed without an action.
 */
@Composable
fun GhostStudentHubLayer(
    isVisible: Boolean,
    position: Offset,
    onActionSelected: (GhostAction) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val hapticManager = remember { GhostHapticManager(context) }

    val actions = remember {
        listOf(
            GhostAction("LOG_BEHAVIOR", Icons.Default.AddReaction, "Log Behavior"),
            GhostAction("NEURAL_INSIGHT", Icons.Default.Psychology, "Neural Insight"),
            GhostAction("NEURAL_SYNAPSE", Icons.Default.Hub, "Neural Synapse"),
            GhostAction("LOG_ACADEMIC", Icons.Default.School, "Log Academic"),
            GhostAction("NEURAL_DOSSIER", Icons.Default.Article, "Neural Dossier"),
            GhostAction("EDIT_STUDENT", Icons.Default.Person, "Edit Student")
        )
    }

    var selectedAngle by remember { mutableFloatStateOf(0f) }
    var highlightAlpha by remember { mutableFloatStateOf(0f) }
    var activeActionIndex by remember { mutableIntStateOf(-1) }

    val transition = updateTransition(targetState = isVisible, label = "student_hub_transition")
    val hubScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "scale"
    ) { if (it) 1f else 0f }

    val hubAlpha by transition.animateFloat(
        transitionSpec = { tween(300) },
        label = "alpha"
    ) { if (it) 1f else 0f }

    if (hubScale <= 0f && !isVisible) return

    val infiniteTransition = rememberInfiniteTransition(label = "student_hub_pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
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
                .offset { IntOffset(position.x.toInt() - 140.dp.roundToPx(), position.y.toInt() - 140.dp.roundToPx()) }
                .size(280.dp)
                .scale(hubScale)
                .alpha(hubAlpha)
        ) {
            // Shader Background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val shader = remember { RuntimeShader(GhostStudentHubShader.NEURAL_STUDENT_HUB) }
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
                val radius = 90.dp

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset {
                            IntOffset(
                                (cos(angle) * radius.toPx()).toInt(),
                                (sin(angle) * radius.toPx()).toInt()
                            )
                        }
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected = index == activeActionIndex
                    val iconScale by animateFloatAsState(if (isSelected) 1.3f else 1.0f, label = "icon_scale")

                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) Color(0xFF6200EE).copy(alpha = 0.3f) else Color.Transparent,
                        modifier = Modifier.fillMaxSize().scale(iconScale)
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            tint = if (isSelected) Color.Cyan else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Low-level gesture detector for the radial hub.
 *
 * BOLT: Optimized to handle the "Press-Hold-Slide" interaction flow. Since the hub is
 * triggered by a long-press, we bypass [awaitFirstDown] and monitor the existing
 * touch stream until release.
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectHubGestures(
    center: Offset,
    onMove: (Float) -> Unit,
    onRelease: () -> Unit
) {
    androidx.compose.foundation.gestures.awaitEachGesture {
        // Since we are triggered via long-press, the finger is already down.
        // We start monitoring the event stream immediately.
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull() ?: break

            if (change.pressed) {
                val pos = change.position
                val dx = pos.x - center.x
                val dy = pos.y - center.y
                onMove(atan2(dy, dx))
            } else {
                // User released contact
                onRelease()
                break
            }
        }
    }
}
