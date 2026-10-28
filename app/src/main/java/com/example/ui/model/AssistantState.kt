package com.example.ui.model

enum class ZyvexStatus {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class LanguageMode(val code: String, val label: String, val speechLocale: String) {
    AUTO("auto", "AUTO", "en-US"),
    ENGLISH("english", "ENGLISH", "en-GB"),
    URDU("urdu", "اردو", "ur-PK"),
    ROMAN_URDU("roman_urdu", "ROMAN URDU", "en-IN")
}

data class ZyvexUiState(
    val status: ZyvexStatus = ZyvexStatus.IDLE,
    val languageMode: LanguageMode = LanguageMode.AUTO,
    val inputText: String = "",
    val currentAudioLevel: Float = 0f,
    val activeSpeechMessageId: Long? = null,
    val errorMessage: String? = null,
    val apiKey: String = "",
    val isApiKeyConfigured: Boolean = false,
    val isAutoSpeakEnabled: Boolean = true,
    val selectedModel: String = "gemini-2.5-flash"
)
