package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechManager
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.repository.AssistantRepository
import com.example.ui.model.LanguageMode
import com.example.ui.model.ZyvexStatus
import com.example.ui.model.ZyvexUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ZyvexViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AssistantRepository(application, database.chatMessageDao())

    private val _uiState = MutableStateFlow(
        ZyvexUiState(
            apiKey = repository.getApiKey(),
            isApiKeyConfigured = repository.getApiKey().isNotBlank(),
            selectedModel = repository.getSelectedModel()
        )
    )
    val uiState: StateFlow<ZyvexUiState> = _uiState.asStateFlow()

    val messages: StateFlow<List<ChatMessageEntity>> = repository.messagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var speechManager: SpeechManager? = null

    init {
        speechManager = SpeechManager(
            context = application,
            onSpeechRecognized = { text ->
                handleUserSpokenText(text)
            },
            onListeningStateChanged = { isListening ->
                _uiState.value = _uiState.value.copy(
                    status = if (isListening) ZyvexStatus.LISTENING else {
                        if (_uiState.value.status == ZyvexStatus.LISTENING) ZyvexStatus.IDLE else _uiState.value.status
                    }
                )
            },
            onSpeakingStateChanged = { isSpeaking ->
                _uiState.value = _uiState.value.copy(
                    status = if (isSpeaking) ZyvexStatus.SPEAKING else {
                        if (_uiState.value.status == ZyvexStatus.SPEAKING) ZyvexStatus.IDLE else _uiState.value.status
                    },
                    activeSpeechMessageId = if (isSpeaking) _uiState.value.activeSpeechMessageId else null
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    status = ZyvexStatus.ERROR,
                    errorMessage = error
                )
            }
        )

        // Observe audio level for HUD Arc Reactor animation
        viewModelScope.launch {
            speechManager?.audioRmsLevel?.collect { rms ->
                _uiState.value = _uiState.value.copy(currentAudioLevel = rms)
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun setLanguageMode(mode: LanguageMode) {
        _uiState.value = _uiState.value.copy(languageMode = mode)
    }

    fun toggleAutoSpeak(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAutoSpeakEnabled = enabled)
    }

    fun saveApiKey(key: String) {
        repository.setCustomApiKey(key)
        val activeKey = repository.getApiKey()
        _uiState.value = _uiState.value.copy(
            apiKey = activeKey,
            isApiKeyConfigured = activeKey.isNotBlank(),
            errorMessage = null
        )
    }

    fun setSelectedModel(model: String) {
        repository.setSelectedModel(model)
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            status = if (_uiState.value.status == ZyvexStatus.ERROR) ZyvexStatus.IDLE else _uiState.value.status
        )
    }

    fun onMicClicked(hasRecordAudioPermission: Boolean, onRequestPermission: () -> Unit) {
        if (!hasRecordAudioPermission) {
            onRequestPermission()
            return
        }

        when (_uiState.value.status) {
            ZyvexStatus.LISTENING -> {
                speechManager?.stopListening()
            }
            ZyvexStatus.SPEAKING -> {
                speechManager?.stopSpeaking()
            }
            ZyvexStatus.THINKING -> {
                // Currently computing
            }
            else -> {
                val locale = _uiState.value.languageMode.speechLocale
                speechManager?.startListening(locale)
            }
        }
    }

    private fun handleUserSpokenText(spokenText: String) {
        if (spokenText.isBlank()) return
        sendMessage(spokenText)
    }

    fun sendCurrentInput() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        _uiState.value = _uiState.value.copy(inputText = "")
        sendMessage(text)
    }

    fun sendMessage(prompt: String) {
        viewModelScope.launch {
            speechManager?.stopSpeaking()
            speechManager?.stopListening()

            // 1. Save user message to database
            repository.saveMessage(
                text = prompt,
                isUser = true,
                language = _uiState.value.languageMode.code
            )

            // 2. Set thinking state
            _uiState.value = _uiState.value.copy(
                status = ZyvexStatus.THINKING,
                errorMessage = null
            )

            // 3. Query Gemini AI API
            val result = repository.askAssistant(
                userPrompt = prompt,
                languagePreference = _uiState.value.languageMode.code
            )

            result.onSuccess { aiResponse ->
                // Save assistant message to database
                val msgId = repository.saveMessage(
                    text = aiResponse,
                    isUser = false,
                    language = _uiState.value.languageMode.code
                )

                if (_uiState.value.isAutoSpeakEnabled) {
                    val isUrdu = _uiState.value.languageMode == LanguageMode.URDU
                    _uiState.value = _uiState.value.copy(
                        status = ZyvexStatus.SPEAKING,
                        activeSpeechMessageId = msgId
                    )
                    speechManager?.speak(aiResponse, isUrduText = isUrdu)
                } else {
                    _uiState.value = _uiState.value.copy(status = ZyvexStatus.IDLE)
                }
            }.onFailure { error ->
                val errorMsg = error.message ?: "Failed to receive neural telemetry."
                _uiState.value = _uiState.value.copy(
                    status = ZyvexStatus.ERROR,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun speakMessage(message: ChatMessageEntity) {
        _uiState.value = _uiState.value.copy(
            activeSpeechMessageId = message.id,
            status = ZyvexStatus.SPEAKING
        )
        val isUrdu = message.language == "urdu"
        speechManager?.speak(message.text, isUrduText = isUrdu)
    }

    fun stopSpeaking() {
        speechManager?.stopSpeaking()
        _uiState.value = _uiState.value.copy(
            activeSpeechMessageId = null,
            status = ZyvexStatus.IDLE
        )
    }

    fun clearChat() {
        viewModelScope.launch {
            stopSpeaking()
            speechManager?.stopListening()
            repository.clearHistory()
            _uiState.value = _uiState.value.copy(status = ZyvexStatus.IDLE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager?.destroy()
        speechManager = null
    }
}
