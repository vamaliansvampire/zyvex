package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.ClearChatConfirmDialog
import com.example.ui.components.HudCornerBox
import com.example.ui.components.HudQuickActionChips
import com.example.ui.components.HudTelemetryBar
import com.example.ui.components.HudWaveformVisualizer
import com.example.ui.model.LanguageMode
import com.example.ui.model.ZyvexStatus
import com.example.ui.theme.HudCyan
import com.example.ui.theme.HudRedAlert
import com.example.ui.theme.MetallicGold100
import com.example.ui.theme.MetallicGold200
import com.example.ui.theme.MetallicGold300
import com.example.ui.theme.MetallicGold400
import com.example.ui.theme.MetallicGold500
import com.example.ui.theme.MetallicGold600
import com.example.ui.theme.MetallicGold700
import com.example.ui.theme.MetallicGold800
import com.example.ui.theme.RoyalBlack500
import com.example.ui.theme.RoyalBlack600
import com.example.ui.theme.RoyalBlack700
import com.example.ui.theme.RoyalBlack800
import com.example.ui.theme.RoyalBlack900

@Composable
fun ZyvexMainScreen(
    viewModel: ZyvexViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordAudioPermission = isGranted
        if (isGranted) {
            viewModel.onMicClicked(true) {}
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required for voice commands.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(RoyalBlack900)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        containerColor = RoyalBlack900
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Futuristic Header & HUD Title
            TopHudBar(
                languageMode = uiState.languageMode,
                isAutoSpeakEnabled = uiState.isAutoSpeakEnabled,
                isKeyConfigured = uiState.isApiKeyConfigured,
                onLanguageSelected = { viewModel.setLanguageMode(it) },
                onToggleAutoSpeak = { viewModel.toggleAutoSpeak(!uiState.isAutoSpeakEnabled) },
                onOpenSettings = { showSettingsDialog = true },
                onClearChatClicked = { showClearChatDialog = true }
            )

            // 2. HUD Telemetry Strip
            HudTelemetryBar(
                status = uiState.status,
                activeModel = uiState.selectedModel
            )

            // 3. Error / API Key Banner (if any)
            AnimatedVisibility(
                visible = uiState.errorMessage != null || !uiState.isApiKeyConfigured,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (uiState.errorMessage != null) {
                    HudErrorMessage(
                        message = uiState.errorMessage ?: "",
                        onDismiss = { viewModel.dismissError() },
                        onConfigureKey = { showSettingsDialog = true }
                    )
                } else if (!uiState.isApiKeyConfigured) {
                    ApiKeyBanner(
                        onOpenSettings = { showSettingsDialog = true }
                    )
                }
            }

            // 4. Central Arc Reactor & Status Hero (Collapsible / Top Section)
            ArcReactorHudHeader(
                status = uiState.status,
                audioLevel = uiState.currentAudioLevel,
                onReactorClicked = {
                    viewModel.onMicClicked(hasRecordAudioPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            // 5. Chat History List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    EmptyChatGreeting(
                        onPromptSelected = { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("chat_messages_list")
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatMessageItem(
                                message = message,
                                isSpeaking = uiState.activeSpeechMessageId == message.id,
                                onSpeakClicked = { viewModel.speakMessage(it) },
                                onStopSpeakClicked = { viewModel.stopSpeaking() }
                            )
                        }
                    }
                }
            }

            // 6. Quick Action Suggestion Chips
            HudQuickActionChips(
                onChipSelected = { prompt ->
                    viewModel.sendMessage(prompt)
                }
            )

            // 7. Bottom Input Bar with Central Mic
            BottomCommandInputBar(
                inputText = uiState.inputText,
                status = uiState.status,
                onTextChanged = { viewModel.onInputTextChanged(it) },
                onSendClicked = { viewModel.sendCurrentInput() },
                onMicClicked = {
                    viewModel.onMicClicked(hasRecordAudioPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    }

    if (showSettingsDialog) {
        ApiKeyDialog(
            currentApiKey = uiState.apiKey,
            isKeyConfigured = uiState.isApiKeyConfigured,
            selectedModel = uiState.selectedModel,
            isAutoSpeakEnabled = uiState.isAutoSpeakEnabled,
            onSaveApiKey = { viewModel.saveApiKey(it) },
            onModelSelected = { viewModel.setSelectedModel(it) },
            onToggleAutoSpeak = { viewModel.toggleAutoSpeak(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showClearChatDialog) {
        ClearChatConfirmDialog(
            onConfirmClear = { viewModel.clearChat() },
            onDismiss = { showClearChatDialog = false }
        )
    }
}

@Composable
private fun TopHudBar(
    languageMode: LanguageMode,
    isAutoSpeakEnabled: Boolean,
    isKeyConfigured: Boolean,
    onLanguageSelected: (LanguageMode) -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearChatClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RoyalBlack900)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ZYVEX",
                        style = MaterialTheme.typography.titleLarge,
                        color = MetallicGold300,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.titleLarge,
                        color = HudCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "YOUR PERSONAL AI // SYSTEM V4.2",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicGold600,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }

            // Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Auto TTS Toggle
                IconButton(
                    onClick = onToggleAutoSpeak,
                    modifier = Modifier.testTag("toggle_auto_speak_button")
                ) {
                    Icon(
                        imageVector = if (isAutoSpeakEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle auto speech readout",
                        tint = if (isAutoSpeakEnabled) MetallicGold300 else MetallicGold700,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Clear History
                IconButton(
                    onClick = onClearChatClicked,
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear chat history",
                        tint = MetallicGold500,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings / Key
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = if (isKeyConfigured) Icons.Filled.Settings else Icons.Filled.Key,
                        contentDescription = "Settings and API Key",
                        tint = if (isKeyConfigured) MetallicGold300 else HudCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Language Selection Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LANG:",
                style = MaterialTheme.typography.labelSmall,
                color = MetallicGold600,
                fontSize = 10.sp
            )
            LanguageMode.values().forEach { mode ->
                val isSelected = mode == languageMode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) MetallicGold400 else RoyalBlack700)
                        .border(
                            0.5.dp,
                            if (isSelected) MetallicGold200 else MetallicGold700,
                            RoundedCornerShape(3.dp)
                        )
                        .clickable { onLanguageSelected(mode) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("lang_chip_${mode.code}")
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) RoyalBlack900 else MetallicGold200,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcReactorHudHeader(
    status: ZyvexStatus,
    audioLevel: Float,
    onReactorClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Arc Reactor
        ArcReactorCore(
            status = status,
            audioLevel = audioLevel,
            onClick = onReactorClicked,
            size = 130.dp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // State indicator text
        val statusText = when (status) {
            ZyvexStatus.IDLE -> "ALL SYSTEMS OPERATIONAL // TAP TO COMMAND"
            ZyvexStatus.LISTENING -> "LISTENING TO VOICE SENSOR..."
            ZyvexStatus.THINKING -> "PROCESSING NEURAL TELEMETRY..."
            ZyvexStatus.SPEAKING -> "TRANSMITTING RESPONSE..."
            ZyvexStatus.ERROR -> "SYSTEM ALERT // CHECK TELEMETRY"
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = when (status) {
                ZyvexStatus.LISTENING -> HudCyan
                ZyvexStatus.THINKING -> MetallicGold300
                ZyvexStatus.SPEAKING -> MetallicGold200
                ZyvexStatus.ERROR -> HudRedAlert
                else -> MetallicGold500
            },
            fontSize = 10.sp,
            letterSpacing = 1.2.sp
        )

        // Waveform Visualizer
        HudWaveformVisualizer(
            audioLevel = audioLevel,
            status = status,
            modifier = Modifier
                .width(160.dp)
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun ApiKeyBanner(
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(RoyalBlack700)
            .border(1.dp, MetallicGold500, RoundedCornerShape(4.dp))
            .clickable { onOpenSettings() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Key,
                contentDescription = null,
                tint = MetallicGold300,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "GEMINI API KEY REQUIRED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetallicGold300,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to configure or set in AI Studio Secrets panel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetallicGold600,
                    fontSize = 10.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(MetallicGold400)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "SETUP",
                style = MaterialTheme.typography.labelSmall,
                color = RoyalBlack900,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HudErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    onConfigureKey: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(RoyalBlack700)
            .border(1.dp, HudRedAlert, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = HudRedAlert,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontSize = 11.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (message.contains("API_KEY", ignoreCase = true) || message.contains("key", ignoreCase = true)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(MetallicGold500)
                        .clickable { onConfigureKey() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "CONFIG",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalBlack900,
                        fontSize = 9.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss error",
                    tint = MetallicGold600,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyChatGreeting(
    onPromptSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HudCornerBox(
            bracketColor = MetallicGold400,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MetallicGold600.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
        ) {
            Column(
                modifier = Modifier
                    .background(RoyalBlack800.copy(alpha = 0.95f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ZYVEX AI NEURAL LINK",
                    style = MaterialTheme.typography.titleMedium,
                    color = MetallicGold300,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"Good day, Sir. All ZYVEX systems are online and standing by. Speak your command using the central Arc Reactor or type your instruction below. I am fluent in English, Urdu (اردو), and Roman Urdu.\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MetallicGold100,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TelemetryTag(label = "VOICE ENGINE", value = "READY")
                    TelemetryTag(label = "TTS SYNTH", value = "ACTIVE")
                    TelemetryTag(label = "SECURITY", value = "SECURE")
                }
            }
        }
    }
}

@Composable
private fun TelemetryTag(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MetallicGold600,
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = HudCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BottomCommandInputBar(
    inputText: String,
    status: ZyvexStatus,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onMicClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RoyalBlack900,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text input field
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                placeholder = {
                    Text(
                        text = "Command ZYVEX AI...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MetallicGold700
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClicked() }),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MetallicGold400,
                    unfocusedBorderColor = MetallicGold700,
                    focusedTextColor = MetallicGold100,
                    unfocusedTextColor = MetallicGold200,
                    cursorColor = MetallicGold300,
                    focusedContainerColor = RoyalBlack700,
                    unfocusedContainerColor = RoyalBlack700
                ),
                trailingIcon = {
                    if (status == ZyvexStatus.THINKING) {
                        CircularProgressIndicator(
                            color = MetallicGold300,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Microphone Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (status == ZyvexStatus.LISTENING) HudCyan else RoyalBlack600
                    )
                    .border(
                        1.dp,
                        if (status == ZyvexStatus.LISTENING) HudCyan else MetallicGold500,
                        CircleShape
                    )
                    .clickable { onMicClicked() }
                    .testTag("mic_input_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (status == ZyvexStatus.LISTENING) Icons.Filled.Mic else Icons.Filled.MicNone,
                    contentDescription = "Voice input",
                    tint = if (status == ZyvexStatus.LISTENING) RoyalBlack900 else MetallicGold300,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank()) MetallicGold400 else RoyalBlack700
                    )
                    .border(
                        1.dp,
                        if (inputText.isNotBlank()) MetallicGold200 else RoyalBlack500,
                        CircleShape
                    )
                    .clickable(enabled = inputText.isNotBlank() && status != ZyvexStatus.THINKING) {
                        onSendClicked()
                    }
                    .testTag("send_command_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send command",
                    tint = if (inputText.isNotBlank()) RoyalBlack900 else MetallicGold700,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
