package dev.johnoreilly.confetti.agent

import ai.koog.embeddings.base.Embedder
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import dev.johnoreilly.confetti.BuildKonfig

actual fun getLLModel() = GoogleModels.Gemini2_5Flash

actual fun getPromptExecutor(): PromptExecutor {
    return simpleGoogleAIExecutor(BuildKonfig.GEMINI_API_KEY)
}

actual fun getEmbedder(): Embedder = ApiEmbedder(
    provider = GoogleLLMClient(BuildKonfig.GEMINI_API_KEY),
    model = GoogleModels.Embeddings.GeminiEmbedding001,
)

actual fun getEmbeddingCache(): EmbeddingCache? {
    val home = System.getProperty("user.home") ?: return null
    val root = okio.Path.Companion.run { "$home/.cache/confetti-embeddings".toPath() }
    return EmbeddingCache(okio.FileSystem.SYSTEM, root)
}
