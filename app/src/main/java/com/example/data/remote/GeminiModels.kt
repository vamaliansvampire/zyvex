package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents")
    val contents: List<ContentItem>,
    @Json(name = "system_instruction")
    val systemInstruction: ContentItem? = null,
    @Json(name = "generationConfig")
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @Json(name = "role")
    val role: String? = null,
    @Json(name = "parts")
    val parts: List<PartItem>
)

@JsonClass(generateAdapter = true)
data class PartItem(
    @Json(name = "text")
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature")
    val temperature: Float = 0.7f,
    @Json(name = "maxOutputTokens")
    val maxOutputTokens: Int = 2048,
    @Json(name = "topP")
    val topP: Float = 0.95f
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates")
    val candidates: List<CandidateItem>? = null,
    @Json(name = "error")
    val error: GeminiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    @Json(name = "content")
    val content: ContentItem?,
    @Json(name = "finishReason")
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorDetails(
    @Json(name = "code")
    val code: Int?,
    @Json(name = "message")
    val message: String?,
    @Json(name = "status")
    val status: String?
)
