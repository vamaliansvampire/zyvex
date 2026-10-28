package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HudCyan
import com.example.ui.theme.HudGreenNormal
import com.example.ui.theme.MetallicGold100
import com.example.ui.theme.MetallicGold200
import com.example.ui.theme.MetallicGold300
import com.example.ui.theme.MetallicGold400
import com.example.ui.theme.MetallicGold500
import com.example.ui.theme.MetallicGold600
import com.example.ui.theme.MetallicGold700
import com.example.ui.theme.RoyalBlack500
import com.example.ui.theme.RoyalBlack600
import com.example.ui.theme.RoyalBlack700
import com.example.ui.theme.RoyalBlack800
import com.example.ui.theme.RoyalBlack900

@Composable
fun ApiKeyDialog(
    currentApiKey: String,
    isKeyConfigured: Boolean,
    selectedModel: String,
    isAutoSpeakEnabled: Boolean,
    onSaveApiKey: (String) -> Unit,
    onModelSelected: (String) -> Unit,
    onToggleAutoSpeak: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var keyInput by remember { mutableStateOf(currentApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    val models = listOf(
        "gemini-2.5-flash" to "Gemini 2.5 Flash (Recommended)",
        "gemini-2.0-flash" to "Gemini 2.0 Flash",
        "gemini-1.5-flash" to "Gemini 1.5 Flash"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("settings_dialog"),
        containerColor = RoyalBlack800,
        tonalElevation = 8.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = MetallicGold300,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SYSTEM CONFIGURATION",
                    style = MaterialTheme.typography.titleMedium,
                    color = MetallicGold200
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Key status badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(RoyalBlack700)
                        .border(1.dp, if (isKeyConfigured) HudGreenNormal else MetallicGold700, RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isKeyConfigured) HudGreenNormal else Color(0xFFFF9900))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isKeyConfigured) "NEURAL LINK: CONNECTED" else "KEY REQUIRED // STANDBY",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isKeyConfigured) HudGreenNormal else Color(0xFFFF9900),
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isKeyConfigured) "API Key is active and ready" else "Provide a Gemini key to activate intelligence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MetallicGold600,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "GEMINI API KEY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicGold300,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field"),
                    placeholder = {
                        Text("AIzaSy...", color = MetallicGold700, fontSize = 12.sp)
                    },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle key visibility",
                                tint = MetallicGold500
                            )
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetallicGold400,
                        unfocusedBorderColor = MetallicGold700,
                        focusedTextColor = MetallicGold100,
                        unfocusedTextColor = MetallicGold200,
                        cursorColor = MetallicGold300
                    )
                )

                Text(
                    text = "ℹ️ Get a free API key at ai.google.dev or configure in the AI Studio Secrets panel. Your key remains private on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetallicGold600,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Model Selector
                Text(
                    text = "NEURAL MODEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicGold300,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                models.forEach { (modelId, modelName) ->
                    val isSelected = modelId == selectedModel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) RoyalBlack600 else Color.Transparent)
                            .border(
                                0.5.dp,
                                if (isSelected) MetallicGold400 else RoyalBlack500,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onModelSelected(modelId) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MetallicGold300 else MetallicGold700)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = modelName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MetallicGold200 else MetallicGold600,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Auto-TTS switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AUTO VOICE READOUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetallicGold300,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Speak AI replies immediately aloud",
                            style = MaterialTheme.typography.bodySmall,
                            color = MetallicGold600,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = isAutoSpeakEnabled,
                        onCheckedChange = onToggleAutoSpeak,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MetallicGold300,
                            checkedTrackColor = MetallicGold700,
                            uncheckedThumbColor = MetallicGold700,
                            uncheckedTrackColor = RoyalBlack700
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveApiKey(keyInput.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MetallicGold400,
                    contentColor = RoyalBlack900
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("SAVE & ACTIVATE", style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = MetallicGold500, style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Composable
fun ClearChatConfirmDialog(
    onConfirmClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoyalBlack800,
        title = {
            Text(
                text = "PURGE TELEMETRY LOGS?",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFF5555)
            )
        },
        text = {
            Text(
                text = "This will erase all active conversation history from local database memory. Are you sure, Sir?",
                style = MaterialTheme.typography.bodyMedium,
                color = MetallicGold200
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmClear()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCC2222),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("confirm_clear_chat_button")
            ) {
                Text("PURGE LOGS", style = MaterialTheme.typography.labelSmall)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MetallicGold500, style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}
