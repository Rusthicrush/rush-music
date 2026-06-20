package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Data structures for JSON serialization
    data class TextPart(val text: String)
    data class Content(val parts: List<TextPart>)
    data class RequestBody(val contents: List<Content>, val systemInstruction: Content? = null)
    
    data class CandidateParts(val text: String?)
    data class CandidateContent(val parts: List<CandidateParts>)
    data class Candidate(val content: CandidateContent)
    data class ResponseBody(val candidates: List<Candidate>?)

    suspend fun getCharacterResponse(userMessage: String, systemPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured or empty.")
            return@withContext "I am listening to your heartbeat in the silence of the night. Please enter your API key in AI Studio to let me speak more clearly. (Currently playing serene Am9 offline chords for you.)"
        }

        try {
            val jsonAdapter = moshi.adapter(RequestBody::class.java)
            val reqObj = RequestBody(
                contents = listOf(Content(parts = listOf(TextPart(text = userMessage)))),
                systemInstruction = Content(parts = listOf(TextPart(text = systemPrompt)))
            )
            val jsonString = jsonAdapter.toJson(reqObj)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    Log.e(TAG, "API failed with code ${response.code}: $bodyStr")
                    return@withContext getLocalFallbackMessage(userMessage)
                }

                val respStr = response.body?.string() ?: ""
                val respAdapter = moshi.adapter(ResponseBody::class.java)
                val respObj = respAdapter.fromJson(respStr)
                val text = respObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                text ?: "I heard your thoughts. Let the music bring peace to your mind."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            getLocalFallbackMessage(userMessage)
        }
    }

    private fun getLocalFallbackMessage(userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("sad") || lower.contains("pain") || lower.contains("hurt") -> {
                "Life is full of heavy moments, like a dark sky before the stars appear. I am here with you. Let the soft, slow chords of this midnight melody wrap around your heart. You are stronger than you think."
            }
            lower.contains("lonely") || lower.contains("alone") -> {
                "Solitude is not empty; it is a canvas waiting for your inner light to shine. Even in the deepest void, the stars are anchored. I am right here. Listen closely to the gentle pulses."
            }
            lower.contains("happy") || lower.contains("joy") || lower.contains("excit") -> {
                "I am glad your sky is shining bright tonight. Cherish these warm rays, but let's keep the core steady and soft. This rhythm celebrates your peaceful joy."
            }
            lower.contains("stress") || lower.contains("anxious") || lower.contains("tired") -> {
                "Take a deep breath. Let go of the day's weight. No flashy colors, no loud voices. Just you, me, and this calm, repeating synth pulsing slowly. Breathe in sync with the waveform."
            }
            else -> {
                "Some feelings cannot be put into words. That is why we have music. Just relax your mind, and let each sound carry you into a peaceful slumber tonight."
            }
        }
    }
}
