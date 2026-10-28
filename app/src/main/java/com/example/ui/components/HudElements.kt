package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.ZyvexStatus
import com.example.ui.theme.HudCyan
import com.example.ui.theme.HudGreenNormal
import com.example.ui.theme.HudRedAlert
import com.example.ui.theme.MetallicGold200
import com.example.ui.theme.MetallicGold300
import com.example.ui.theme.MetallicGold400
import com.example.ui.theme.MetallicGold600
import com.example.ui.theme.MetallicGold700
import com.example.ui.theme.RoyalBlack500
import com.example.ui.theme.RoyalBlack700
import com.example.ui.theme.RoyalBlack800

@Composable
fun HudCornerBox(
    modifier: Modifier = Modifier,
    bracketColor: Color = MetallicGold400,
    bracketLength: Dp = 10.dp,
    strokeWidth: Dp = 1.5.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(RoyalBlack700.copy(alpha = 0.85f))
            .border(0.5.dp, RoyalBlack500, RoundedCornerShape(4.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val len = bracketLength.toPx()
            val sw = strokeWidth.toPx()
            val w = size.width
            val h = size.height

            // Top-left bracket
            drawLine(bracketColor, Offset(0f, 0f), Offset(len, 0f), sw, StrokeCap.Square)
            drawLine(bracketColor, Offset(0f, 0f), Offset(0f, len), sw, StrokeCap.Square)

            // Top-right bracket
            drawLine(bracketColor, Offset(w, 0f), Offset(w - len, 0f), sw, StrokeCap.Square)
            drawLine(bracketColor, Offset(w, 0f), Offset(w, len), sw, StrokeCap.Square)

            // Bottom-left bracket
            drawLine(bracketColor, Offset(0f, h), Offset(len, h), sw, StrokeCap.Square)
            drawLine(bracketColor, Offset(0f, h), Offset(0f, h - len), sw, StrokeCap.Square)

            // Bottom-right bracket
            drawLine(bracketColor, Offset(w, h), Offset(w - len, h), sw, StrokeCap.Square)
            drawLine(bracketColor, Offset(w, h), Offset(w, h - len), sw, StrokeCap.Square)
        }
        content()
    }
}

@Composable
fun HudTelemetryBar(
    status: ZyvexStatus,
    activeModel: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TelemetryBlink")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TelemetryDotAlpha"
    )

    val (statusColor, statusLabel) = when (status) {
        ZyvexStatus.IDLE -> Pair(HudGreenNormal, "SYS.NOMINAL // READY")
        ZyvexStatus.LISTENING -> Pair(HudCyan, "AUDIO.SENSOR // RECEIVING")
        ZyvexStatus.THINKING -> Pair(MetallicGold300, "NEURAL.CORE // COMPUTING")
        ZyvexStatus.SPEAKING -> Pair(MetallicGold200, "VOICE.SYNTH // TRANSMITTING")
        ZyvexStatus.ERROR -> Pair(HudRedAlert, "SYS.ALERT // CHECK TELEMETRY")
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RoyalBlack800.copy(alpha = 0.95f))
            .border(0.5.dp, MetallicGold700.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = statusColor.copy(alpha = dotAlpha))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontSize = 10.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "CORE: $activeModel",
                style = MaterialTheme.typography.labelSmall,
                color = MetallicGold600,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FPS: 60",
                style = MaterialTheme.typography.labelSmall,
                color = MetallicGold600,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun HudWaveformVisualizer(
    audioLevel: Float,
    status: ZyvexStatus,
    modifier: Modifier = Modifier,
    barCount: Int = 16
) {
    Row(
        modifier = modifier.height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val factor = if (status == ZyvexStatus.LISTENING || status == ZyvexStatus.SPEAKING) {
                // Wave modulation
                val centerOffset = kotlin.math.abs(i - (barCount / 2f)) / (barCount / 2f)
                val base = (1.0f - (centerOffset * 0.5f))
                val wave = (audioLevel * base).coerceIn(0.12f, 1.0f)
                wave
            } else if (status == ZyvexStatus.THINKING) {
                val cycle = ((System.currentTimeMillis() / 150 + i) % 5) / 5f
                0.2f + (cycle * 0.5f)
            } else {
                0.1f
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((24 * factor).dp.coerceAtLeast(3.dp))
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        when (status) {
                            ZyvexStatus.LISTENING -> HudCyan
                            ZyvexStatus.THINKING -> MetallicGold300
                            ZyvexStatus.SPEAKING -> MetallicGold200
                            else -> MetallicGold700
                        }
                    )
            )
        }
    }
}

@Composable
fun HudQuickActionChips(
    onChipSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickActions = listOf(
        "💡 System Diagnostic" to "ZYVEX, run a full system and telemetry diagnostic.",
        "🇵🇰 اردو میں حال بتائیں" to "کیا حال ہے؟ اپنے بارے میں تفصیل سے اردو میں بتائیں۔",
        "⚡ Tactical Briefing" to "Provide a tactical executive briefing of today's key priorities.",
        "✍️ Draft Formal Email" to "Draft a polite, professional executive email confirming the project deployment.",
        "🔬 Explain Quantum Physics" to "Explain quantum entanglement in simple, fascinating terms.",
        "💻 Python Code" to "Write a clean Python script for automated system health monitoring."
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for ((label, prompt) in quickActions) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(RoyalBlack700)
                    .border(1.dp, MetallicGold600.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onChipSelected(prompt) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MetallicGold200,
                    fontSize = 11.sp
                )
            }
        }
    }
}
