package dev.johnoreilly.confetti.prompt

import dev.johnoreilly.confetti.ai.OnDeviceAI

class PromptApiAndroid(private val onDeviceAI: OnDeviceAI): PromptApi {
    override suspend fun generateContent(prompt: String, query: String): PromptResponse {
        val fullPrompt = "$prompt\n\n$query"
        val response = onDeviceAI.generateContent(fullPrompt)
        return PromptResponse(response)
    }
}
