package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ChatMessageDao
import com.example.data.local.ChatMessageEntity
import com.example.data.remote.CandidateItem
import com.example.data.remote.ContentItem
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GenerationConfig
import com.example.data.remote.PartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssistantRepository(
    private val context: Context,
    private val chatMessageDao: ChatMessageDao,
    private val apiService: GeminiApiService = GeminiApiService.create()
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("zyvex_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "AssistantRepository"
        private const val PREF_CUSTOM_API_KEY = "custom_api_key"
        private const val PREF_SELECTED_MODEL = "selected_model"
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
    }

    val messagesFlow: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessagesFlow()

    fun getApiKey(): String {
        val customKey = prefs.getString(PREF_CUSTOM_API_KEY, "")?.trim().orEmpty()
        if (customKey.isNotEmpty()) return customKey

        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Throwable) {
            ""
        }
        return if (buildKey == "MY_GEMINI_API_KEY") "" else buildKey
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(PREF_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun getSelectedModel(): String {
        return prefs.getString(PREF_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(PREF_SELECTED_MODEL, model).apply()
    }

    suspend fun saveMessage(text: String, isUser: Boolean, language: String = "en"): Long {
        return withContext(Dispatchers.IO) {
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    text = text,
                    isUser = isUser,
                    timestamp = System.currentTimeMillis(),
                    language = language,
                    modelUsed = getSelectedModel()
                )
            )
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            chatMessageDao.clearAllMessages()
        }
    }

    suspend fun deleteMessage(message: ChatMessageEntity) {
        withContext(Dispatchers.IO) {
            chatMessageDao.deleteMessage(message)
        }
    }

    suspend fun askAssistant(
        userPrompt: String,
        languagePreference: String = "auto"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("API_KEY_MISSING: Please configure your Gemini API Key in Settings or the AI Studio Secrets panel.")
            )
        }

        try {
            // Retrieve recent messages for context
            val recentMessages = chatMessageDao.getRecentMessages(6).reversed()

            val contents = mutableListOf<ContentItem>()
            for (msg in recentMessages) {
                val role = if (msg.isUser) "user" else "model"
                contents.add(ContentItem(role = role, parts = listOf(PartItem(text = msg.text))))
            }
            // Append current user prompt
            contents.add(ContentItem(role = "user", parts = listOf(PartItem(text = userPrompt))))

            val languageInstruction = when (languagePreference.lowercase()) {
                "urdu" -> "The user prefers responses in Urdu (اردو). Respond strictly in fluent, respectful Urdu script."
                "roman_urdu" -> "The user prefers responses in Roman Urdu (Urdu written in Latin alphabet, e.g., 'Aapka hukum, Janab'). Respond naturally in Roman Urdu."
                "english" -> "Respond in refined British-accented JARVIS English."
                else -> "Detect the user's input language. If they ask in Urdu (اردو), reply in Urdu. If in Roman Urdu (e.g. 'kya haal hai'), reply in natural Roman Urdu. If in English, reply in sophisticated JARVIS English."
            }

            val systemPrompt = """
                You are ZYVEX AI, an elite, ultra-intelligent personal AI assistant inspired by Iron Man's JARVIS.
                Your Persona:
                - Impeccably polite, sophisticated, articulate, calm, loyal, with subtle dry wit.
                - Address the user as 'Sir' or 'Ma'am' respectfully when appropriate (e.g., 'At your service, Sir.', 'Certainly, Sir.').
                - Provide razor-sharp intelligence for science, technology, calculations, planning, coding, and general tasks.
                - Keep answers concise, direct, and pleasant for speech synthesis unless deep detail is explicitly requested.
                $languageInstruction
            """.trimIndent()

            val request = GeminiRequest(
                systemInstruction = ContentItem(parts = listOf(PartItem(text = systemPrompt))),
                contents = contents,
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 1500
                )
            )

            val primaryModel = getSelectedModel()
            var response = apiService.generateContent(
                model = primaryModel,
                apiKey = apiKey,
                request = request
            )

            // If 404 on model name, attempt fallback model
            if (!response.isSuccessful && response.code() == 404 && primaryModel != "gemini-1.5-flash") {
                Log.w(TAG, "Primary model $primaryModel returned 404, falling back to gemini-1.5-flash")
                response = apiService.generateContent(
                    model = "gemini-1.5-flash",
                    apiKey = apiKey,
                    request = request
                )
            }

            if (response.isSuccessful) {
                val body = response.body()
                val candidateText = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!candidateText.isNullOrBlank()) {
                    Result.success(candidateText.trim())
                } else {
                    Result.failure(Exception("Model returned an empty response."))
                }
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                Log.e(TAG, "Gemini API error code: ${response.code()} body: $errorBody")
                val userFriendlyMessage = when (response.code()) {
                    400 -> "Invalid request or prompt format. (HTTP 400)"
                    403 -> "Authentication failed: Gemini API key is invalid or quota exceeded. (HTTP 403)"
                    429 -> "Rate limit reached. Please wait a few seconds and retry. (HTTP 429)"
                    else -> "Connection error (HTTP ${response.code()}). Check network telemetry."
                }
                Result.failure(Exception(userFriendlyMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Assistant request failed", e)
            Result.failure(e)
        }
    }
}
