package dev.johnoreilly.confetti.prompt

data class PromptResponse(val text: String?)

interface PromptApi {
    suspend fun generateContent(prompt: String, query: String): PromptResponse
}
