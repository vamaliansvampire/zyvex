package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.model.ZyvexStatus
import com.example.ui.theme.HudCyan
import com.example.ui.theme.MetallicGold100
import com.example.ui.theme.MetallicGold200
import com.example.ui.theme.MetallicGold300
import com.example.ui.theme.MetallicGold400
import com.example.ui.theme.MetallicGold500
import com.example.ui.theme.MetallicGold600
import com.example.ui.theme.MetallicGold700
import com.example.ui.theme.RoyalBlack800
import com.example.ui.theme.RoyalBlack900
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorCore(
    status: ZyvexStatus,
    audioLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorAnimations")

    // Slow clockwise rotation for outer dial
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (status) {
                    ZyvexStatus.THINKING -> 3000
                    ZyvexStatus.LISTENING -> 6000
                    ZyvexStatus.SPEAKING -> 8000
                    else -> 24000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRingRotation"
    )

    // Counter-clockwise rotation for inner technical ring
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (status) {
                    ZyvexStatus.THINKING -> 2000
                    ZyvexStatus.LISTENING -> 4500
                    ZyvexStatus.SPEAKING -> 6000
                    else -> 18000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "InnerRingRotation"
    )

    // Breathing glow animation
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (status) {
                    ZyvexStatus.THINKING -> 500
                    ZyvexStatus.LISTENING -> 800
                    ZyvexStatus.SPEAKING -> 900
                    else -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    // Responsive audio scale factor
    val dynamicAudioScale = remember { Animatable(1f) }
    LaunchedEffect(audioLevel, status) {
        if (status == ZyvexStatus.LISTENING || status == ZyvexStatus.SPEAKING) {
            val target = 1.0f + (audioLevel * 0.28f)
            dynamicAudioScale.animateTo(target, tween(60, easing = LinearEasing))
        } else {
            dynamicAudioScale.animateTo(1f, tween(200))
        }
    }

    val primaryGlowColor = when (status) {
        ZyvexStatus.LISTENING -> HudCyan
        ZyvexStatus.THINKING -> MetallicGold300
        ZyvexStatus.SPEAKING -> MetallicGold200
        ZyvexStatus.ERROR -> Color(0xFFFF3344)
        ZyvexStatus.IDLE -> MetallicGold400
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (status != ZyvexStatus.IDLE) 24.dp else 12.dp,
                shape = CircleShape,
                ambientColor = primaryGlowColor,
                spotColor = primaryGlowColor
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = size / 2, color = MetallicGold300),
                onClick = onClick
            )
            .testTag("arc_reactor_core_button"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) - 6.dp.toPx()
            val effectiveRadius = baseRadius * dynamicAudioScale.value.coerceIn(0.9f, 1.25f)

            // 1. Deep Royal Black base background
            drawCircle(
                color = RoyalBlack900,
                radius = baseRadius,
                center = center
            )

            // 2. Ambient energy glow field
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlowColor.copy(alpha = 0.35f * pulseGlow),
                        MetallicGold500.copy(alpha = 0.12f * pulseGlow),
                        Color.Transparent
                    ),
                    center = center,
                    radius = effectiveRadius
                ),
                radius = effectiveRadius,
                center = center
            )

            // 3. Outermost mechanical chassis ring
            drawCircle(
                color = MetallicGold600.copy(alpha = 0.6f),
                radius = effectiveRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. Segmented Outer Dial with rotating ticks
            rotate(outerRotation, pivot = center) {
                val tickRadius = effectiveRadius - 6.dp.toPx()
                for (i in 0 until 36) {
                    val angle = (i * 10) * (Math.PI / 180.0)
                    val isMajorTick = i % 3 == 0
                    val tickLen = if (isMajorTick) 7.dp.toPx() else 3.dp.toPx()
                    val tickColor = if (isMajorTick) MetallicGold300 else MetallicGold700

                    val startX = (center.x + (tickRadius - tickLen) * cos(angle)).toFloat()
                    val startY = (center.y + (tickRadius - tickLen) * sin(angle)).toFloat()
                    val endX = (center.x + tickRadius * cos(angle)).toFloat()
                    val endY = (center.y + tickRadius * sin(angle)).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajorTick) 1.8.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 3 segmented HUD arcs around perimeter
                val arcRadius = effectiveRadius - 10.dp.toPx()
                for (a in 0 until 3) {
                    val startAngle = a * 120f + 10f
                    drawArc(
                        color = MetallicGold400.copy(alpha = 0.75f),
                        startAngle = startAngle,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 5. Counter-rotating inner technical ring with telemetry notches
            rotate(innerRotation, pivot = center) {
                val midRadius = effectiveRadius * 0.68f
                drawCircle(
                    color = RoyalBlack800,
                    radius = midRadius,
                    center = center
                )
                drawCircle(
                    color = primaryGlowColor.copy(alpha = 0.8f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 8 Arc reactor coil segments
                for (i in 0 until 8) {
                    val angle = (i * 45) * (Math.PI / 180.0)
                    val coilRadius = midRadius - 4.dp.toPx()
                    val startX = (center.x + (coilRadius - 9.dp.toPx()) * cos(angle)).toFloat()
                    val startY = (center.y + (coilRadius - 9.dp.toPx()) * sin(angle)).toFloat()
                    val endX = (center.x + coilRadius * cos(angle)).toFloat()
                    val endY = (center.y + coilRadius * sin(angle)).toFloat()

                    drawLine(
                        color = MetallicGold200,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Square
                    )
                }
            }

            // 6. Central Arc Reactor Core Chamber
            val coreChamberRadius = effectiveRadius * 0.44f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MetallicGold100,
                        primaryGlowColor,
                        MetallicGold600,
                        RoyalBlack800
                    ),
                    center = center,
                    radius = coreChamberRadius
                ),
                radius = coreChamberRadius,
                center = center
            )

            // Core inner ring
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = coreChamberRadius * 0.72f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Central Icon / Status Indicator
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (status) {
                ZyvexStatus.LISTENING -> {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Listening",
                        tint = RoyalBlack900,
                        modifier = Modifier.size(30.dp)
                    )
                }
                ZyvexStatus.THINKING -> {
                    Icon(
                        imageVector = Icons.Filled.MicOff,
                        contentDescription = "Thinking",
                        tint = RoyalBlack900,
                        modifier = Modifier.size(28.dp)
                    )
                }
                ZyvexStatus.SPEAKING -> {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop speaking",
                        tint = RoyalBlack900,
                        modifier = Modifier.size(28.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Tap to speak",
                        tint = RoyalBlack900,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}
