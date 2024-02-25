package dev.johnoreilly.confetti

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.GenerateContentResponse
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlin.io.encoding.ExperimentalEncodingApi

class GeminiApi {
    private val apiKey =  BuildKonfig.GEMINI_API_KEY

    val generativeVisionModel = GenerativeModel(
        modelName = "gemini-pro-vision",
        apiKey = apiKey
    )

    val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey
    )

    fun generateContent(prompt: String): Flow<GenerateContentResponse> {
        return generativeModel.generateContentStream(prompt)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun generateContent(prompt: String, imageData: ByteArray): Flow<GenerateContentResponse> {
        val content = content {
            image(PlatformImage(imageData))
            text(prompt)
        }
        return generativeVisionModel.generateContentStream(content)
    }
}