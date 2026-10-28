package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.HudCyan
import com.example.ui.theme.MetallicGold100
import com.example.ui.theme.MetallicGold200
import com.example.ui.theme.MetallicGold300
import com.example.ui.theme.MetallicGold400
import com.example.ui.theme.MetallicGold500
import com.example.ui.theme.MetallicGold600
import com.example.ui.theme.MetallicGold700
import com.example.ui.theme.RoyalBlack600
import com.example.ui.theme.RoyalBlack700
import com.example.ui.theme.RoyalBlack800
import com.example.ui.theme.RoyalBlack900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    isSpeaking: Boolean,
    onSpeakClicked: (ChatMessageEntity) -> Unit,
    onStopSpeakClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val isUser = message.isUser
    val isUrdu = isArabicScript(message.text)

    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 330.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Header / Telemetry Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 3.dp)
            ) {
                if (!isUser) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) HudCyan else MetallicGold300)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "ZYVEX // CORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MetallicGold300,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Text(
                        text = "USER // COMMAND",
                        style = MaterialTheme.typography.labelSmall,
                        color = MetallicGold500,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MetallicGold500)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicGold700,
                    fontSize = 9.sp
                )
            }

            // Message Bubble Card
            HudCornerBox(
                bracketColor = if (isUser) MetallicGold600 else if (isSpeaking) HudCyan else MetallicGold400,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isUser) MetallicGold700.copy(alpha = 0.5f) else MetallicGold500.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .background(if (isUser) RoyalBlack700.copy(alpha = 0.9f) else RoyalBlack800.copy(alpha = 0.95f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isUser) MetallicGold100 else Color.White,
                        fontSize = if (isUrdu) 17.sp else 15.sp,
                        lineHeight = if (isUrdu) 26.sp else 22.sp,
                        textAlign = if (isUrdu) TextAlign.Right else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Action buttons (Copy & TTS) for AI messages
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Speak / Stop Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSpeaking) HudCyan.copy(alpha = 0.2f) else RoyalBlack600)
                                        .border(
                                            0.5.dp,
                                            if (isSpeaking) HudCyan else MetallicGold600,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            if (isSpeaking) {
                                                onStopSpeakClicked()
                                            } else {
                                                onSpeakClicked(message)
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("tts_message_button_${message.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = if (isSpeaking) "Stop voice readout" else "Read response aloud",
                                            tint = if (isSpeaking) HudCyan else MetallicGold300,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isSpeaking) "STOP AUDIO" else "SPEAK ALOUD",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSpeaking) HudCyan else MetallicGold200,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // One-Tap Copy Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RoyalBlack600)
                                        .border(0.5.dp, MetallicGold600, RoundedCornerShape(4.dp))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(message.text))
                                            isCopied = true
                                            Toast.makeText(context, "Telemetry copied to clipboard", Toast.LENGTH_SHORT).show()
                                            coroutineScope.launch {
                                                delay(2000)
                                                isCopied = false
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("copy_message_button_${message.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                            contentDescription = "Copy message",
                                            tint = if (isCopied) HudCyan else MetallicGold300,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isCopied) "COPIED" else "COPY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isCopied) HudCyan else MetallicGold200,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            // Model tag badge
                            Text(
                                text = "CORE: ${message.modelUsed.takeLast(10)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MetallicGold700,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isArabicScript(text: String): Boolean {
    for (char in text) {
        val block = Character.UnicodeBlock.of(char)
        if (block == Character.UnicodeBlock.ARABIC ||
            block == Character.UnicodeBlock.ARABIC_SUPPLEMENT ||
            block == Character.UnicodeBlock.ARABIC_EXTENDED_A ||
            block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
            block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B
        ) {
            return true
        }
    }
    return false
}
