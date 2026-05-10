package com.example.myapplication.labs.ghost

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.labs.ghost.util.GhostGlassmorphicSurface
import com.example.myapplication.ui.theme.GhostCyan
import com.example.myapplication.ui.theme.GhostMagenta
import kotlin.math.roundToInt

/**
 * GhostDeckLayer: A high-fidelity, swipeable student card stack.
 *
 * It provides a "Ghost Deck" UI for rapid review and logging.
 *
 * @param deck The stack of students to review.
 * @param onLog Action callback when a card is swiped (Right = Positive, Left = Negative).
 * @param onDismiss Close the deck view.
 */
@Composable
fun GhostDeckLayer(
    deck: List<GhostDeckEngine.StudentCard>,
    onLog: (Long, Boolean) -> Unit, // studentId, isPositive
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (deck.isEmpty()) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    // BOLT: Auto-dismiss when stack is exhausted
    LaunchedEffect(currentIndex) {
        if (currentIndex >= deck.size && deck.isNotEmpty()) {
            onDismiss()
        }
    }

    val currentCard = deck.getOrNull(currentIndex) ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "deckTime")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        // Deck HUD Background
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NEURAL DECK 👻",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = GhostCyan,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                "${currentIndex + 1} / ${deck.size}",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        // Swipeable Card Stack
        Box(modifier = Modifier.size(320.dp, 450.dp)) {
            // Next Card Preview (Subtle)
            deck.getOrNull(currentIndex + 1)?.let { nextCard ->
                GhostDeckCard(
                    card = nextCard,
                    time = time,
                    swipeOffset = Offset.Zero,
                    isTop = false,
                    modifier = Modifier.scale(0.9f).offset(y = 20.dp)
                )
            }

            // Top Card
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "offsetX")

            GhostDeckCard(
                card = currentCard,
                time = time,
                swipeOffset = Offset(animatedOffsetX, 0f),
                isTop = true,
                modifier = Modifier
                    .pointerInput(currentCard.student.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                            },
                            onDragEnd = {
                                if (offsetX > 400f) {
                                    onLog(currentCard.student.id.toLong(), true)
                                    currentIndex++
                                    offsetX = 0f
                                } else if (offsetX < -400f) {
                                    onLog(currentCard.student.id.toLong(), false)
                                    currentIndex++
                                    offsetX = 0f
                                } else {
                                    offsetX = 0f
                                }
                            }
                        )
                    }
            )
        }
    }
}

@Composable
fun GhostDeckCard(
    card: GhostDeckEngine.StudentCard,
    time: Float,
    swipeOffset: Offset,
    isTop: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation = (swipeOffset.x / 40f).coerceIn(-15f, 15f)
    val alpha = (1f - (kotlin.math.abs(swipeOffset.x) / 800f)).coerceAtLeast(0.5f)

    GhostGlassmorphicSurface(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = swipeOffset.x
                rotationZ = rotation
                this.alpha = if (isTop) 1f else 0.5f
            }
            .clip(RoundedCornerShape(24.dp)),
        glassmorphismEnabled = true
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. AGSL Shader Background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val shader = remember { RuntimeShader(GhostDeckShader.NEURAL_FLUX_DECK) }
                val brush = remember(shader) { ShaderBrush(shader) }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    shader.setFloatUniform("iSwipe", (swipeOffset.x / 500f).coerceIn(-1f, 1f))
                    shader.setFloatUniform("iAffinity", card.affinity)

                    val colorA = GhostCyan
                    val colorB = GhostMagenta
                    shader.setColorUniform("iColorA", android.graphics.Color.valueOf(colorA.red, colorA.green, colorA.blue, colorA.alpha))
                    shader.setColorUniform("iColorB", android.graphics.Color.valueOf(colorB.red, colorB.green, colorB.blue, colorB.alpha))

                    drawRect(brush = brush)
                }
            }

            // 2. Card Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        card.student.fullName.value,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Text(
                        if (card.focusNeeded) "FOCUS REQUIRED ⚡" else "STABLE STATE 🛡️",
                        color = if (card.focusNeeded) GhostMagenta else GhostCyan,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    DeckMetric("AFFINITY", "${(card.affinity * 100).toInt()}%")
                    DeckMetric("ENGAGEMENT", "${(card.engagementScore * 100).toInt()}%")
                }
            }

            // Swipe Indicators
            if (isTop && kotlin.math.abs(swipeOffset.x) > 50f) {
                val indicatorText = if (swipeOffset.x > 0) "LOG POSITIVE ✔" else "LOG NEGATIVE ✖"
                val indicatorColor = if (swipeOffset.x > 0) GhostCyan else GhostMagenta
                val indicatorAlpha = (kotlin.math.abs(swipeOffset.x) / 300f).coerceAtMost(1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(indicatorColor.copy(alpha = 0.2f * indicatorAlpha)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        indicatorText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = indicatorColor,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        modifier = Modifier.graphicsLayer { this.alpha = indicatorAlpha }
                    )
                }
            }
        }
    }
}

@Composable
fun DeckMetric(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black))
    }
}

private fun Modifier.scale(scale: Float) = graphicsLayer {
    scaleX = scale
    scaleY = scale
}
