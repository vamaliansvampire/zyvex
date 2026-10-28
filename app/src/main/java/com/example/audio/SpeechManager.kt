package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onSpeakingStateChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "SpeechManager"
        private const val UTTERANCE_ID_ZYVEX = "zyvex_utterance"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _audioRmsLevel = MutableStateFlow(0f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        initSpeechRecognizer()
        initTextToSpeech()
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@SpeechManager)
                }
            } else {
                Log.w(TAG, "SpeechRecognizer is not available on this device")
            }
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            // Default to UK English for JARVIS persona
            val result = textToSpeech?.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale.US)
            }
            textToSpeech?.setPitch(0.95f) // slightly deeper, calm butler tone
            textToSpeech?.setSpeechRate(1.02f)

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    onSpeakingStateChanged(false)
                }
            })
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status: $status")
            isTtsReady = false
        }
    }

    fun startListening(languageCode: String = "en-US") {
        mainHandler.post {
            try {
                stopSpeaking()
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
                onListeningStateChanged(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening", e)
                onListeningStateChanged(false)
                onError("Failed to initiate voice sensor: ${e.localizedMessage}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                _audioRmsLevel.value = 0f
                onListeningStateChanged(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping listener", e)
            }
        }
    }

    fun speak(text: String, isUrduText: Boolean = false) {
        if (!isTtsReady || textToSpeech == null) {
            Log.w(TAG, "TTS not ready")
            return
        }

        stopSpeaking()

        // Clean markdown characters for voice output
        val speechText = text
            .replace(Regex("\\*\\*|\\*|_|`{1,3}|#+"), "")
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
            .replace(Regex("[<>]"), "")
            .trim()

        if (speechText.isBlank()) return

        if (isUrduText) {
            val urduLocale = Locale("ur", "PK")
            val supported = textToSpeech?.isLanguageAvailable(urduLocale)
            if (supported != TextToSpeech.LANG_NOT_SUPPORTED && supported != TextToSpeech.LANG_MISSING_DATA) {
                textToSpeech?.setLanguage(urduLocale)
            } else {
                textToSpeech?.setLanguage(Locale.US)
            }
        } else {
            textToSpeech?.setLanguage(Locale.UK)
        }

        textToSpeech?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID_ZYVEX)
    }

    fun stopSpeaking() {
        try {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
            }
            onSpeakingStateChanged(false)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    // RecognitionListener Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        onListeningStateChanged(true)
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        // Normalize typical rms dB (-2 to 10 dB) to 0.0 .. 1.0 range
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _audioRmsLevel.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _audioRmsLevel.value = 0f
        onListeningStateChanged(false)
    }

    override fun onError(error: Int) {
        _audioRmsLevel.value = 0f
        onListeningStateChanged(false)
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection required for speech recognition"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech sensor busy. Please retry."
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Speech recognition error ($error)"
        }
        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            onError(message)
        }
    }

    override fun onResults(results: Bundle?) {
        _audioRmsLevel.value = 0f
        onListeningStateChanged(false)
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            onSpeechRecognized(text)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying SpeechManager", e)
        }
    }
}
