package dev.johnoreilly.confetti

import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.BlockThreshold
import dev.shreyaspatil.ai.client.generativeai.type.Content
import dev.shreyaspatil.ai.client.generativeai.type.GenerateContentResponse
import dev.shreyaspatil.ai.client.generativeai.type.GenerationConfig
import dev.shreyaspatil.ai.client.generativeai.type.HarmCategory
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage
import dev.shreyaspatil.ai.client.generativeai.type.SafetySetting
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow


class GeminiApi {
    private val apiKey =  BuildKonfig.GEMINI_API_KEY

    fun generateContentStream(prompt: String): Flow<GenerateContentResponse> {
        val configBuilder = GenerationConfig.Builder()
        configBuilder.temperature = 0.0f

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = configBuilder.build(),
            safetySettings = listOf(SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH))
        )

        return generativeModel.generateContentStream(prompt)
    }

    suspend fun generateContent(prompt: String, query: String): GenerateContentResponse {
        val configBuilder = GenerationConfig.Builder()
        configBuilder.temperature = 0.0f

        val contentBuilder = Content.Builder()
        contentBuilder.text(prompt)

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            systemInstruction = contentBuilder.build(),
            generationConfig = configBuilder.build(),
            safetySettings = listOf(SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH))
        )

        return generativeModel.generateContent(query)
    }

    fun generateContent(prompt: String, imageData: ByteArray): Flow<GenerateContentResponse> {
        val generativeVisionModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = apiKey
        )

        val content = content {
            image(PlatformImage(imageData))
            text(prompt)
        }
        return generativeVisionModel.generateContentStream(content)
    }
}