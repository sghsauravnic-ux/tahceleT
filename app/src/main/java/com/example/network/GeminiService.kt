package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiRepository {
    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    suspend fun generateReply(prompt: String, systemPrompt: String? = null): String {
        return try {
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return "Gemini connection active, but default secret API Key is unconfigured in Secrets panel. Here's a simulated intelligent response!"
            }
            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = systemPrompt?.let { Content(parts = listOf(Part(text = it))) }
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No response from AI assistant. Try again."
        } catch (e: Exception) {
            e.printStackTrace()
            "AI Assistant encountered an error: ${e.localizedMessage ?: "Network issue"}. (Hint: Verify your internet connection or Gemini API key)."
        }
    }

    suspend fun generateSmartReplies(contextPrompt: String): List<String> {
        val prompt = "Based on the following recent messages, generate exactly 2 short, conversational smart reply suggestions that the user can click to respond (each suggestion should be very brief, under 5 words, separated by a newline). Recent chat context:\n$contextPrompt"
        return try {
            val responseText = generateReply(prompt, "You are a helpful chat typing assistant. Keep suggested replies extremely brief, real, and helpful.")
            val items = responseText.lines()
                .map { it.replace(Regex("^[\\d.\\-\\s\\*]+"), "").trim() }
                .filter { it.isNotEmpty() && it.length < 30 }
            if (items.size >= 2) items.take(2) else listOf("Got it, thanks!", "Let's do that!")
        } catch (e: Exception) {
            listOf("Okay!", "Sounds great!")
        }
    }

    suspend fun translateMessage(text: String, targetLanguage: String): String {
        val prompt = "Translate the following message directly into $targetLanguage. Do not add any explanation or preamble. Message:\n$text"
        return generateReply(prompt, "You are a precise, word-for-word message translator assistant.")
    }

    suspend fun summarizeChat(chatHistory: String): String {
        val prompt = "Analyze and summarize this entire chat conversation into a single, cohesive paragraph. Highlight key topics, agreements, or tasks. Chat history:\n$chatHistory"
        return generateReply(prompt, "You are a brilliant and highly concise chat archiver assistant.")
    }
    
    suspend fun generateImageCaption(presetType: String): String {
        val prompt = "An image containing a $presetType. Generate a catchy, modern caption for this photo that someone can post in a chat room. Keep it under 15 words."
        return generateReply(prompt, "You are a creative photo describer and copywriting assistant.")
    }
}
