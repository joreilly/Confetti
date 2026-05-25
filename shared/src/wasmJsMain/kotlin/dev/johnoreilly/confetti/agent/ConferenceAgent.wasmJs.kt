package dev.johnoreilly.confetti.agent

import ai.koog.embeddings.base.Embedder
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import dev.johnoreilly.confetti.BuildKonfig

actual fun getLLModel() = GoogleModels.Gemini2_5Flash

actual fun getPromptExecutor(): PromptExecutor {
    return simpleGoogleAIExecutor(BuildKonfig.GEMINI_API_KEY, KtorKoogHttpClient.Factory())
}

actual fun getEmbedder(): Embedder = ApiEmbedder(
    provider = GoogleLLMClient(
        apiKey = BuildKonfig.GEMINI_API_KEY,
        httpClientFactory = KtorKoogHttpClient.Factory(),
    ),
    model = GoogleModels.Embeddings.GeminiEmbedding001,
)

actual fun getEmbeddingCache(): EmbeddingCache? = null
