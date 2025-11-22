package dev.johnoreilly.confetti.prompt

import dev.johnoreilly.confetti.GeminiApi

class PromptApiJvm(private val geminiApi: GeminiApi): PromptApi {
    override suspend fun generateContent(prompt: String, query: String): PromptResponse {
        val response = geminiApi.generateContent(prompt, query)
        return PromptResponse(response.text)
    }
}
