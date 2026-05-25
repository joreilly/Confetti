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

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getEmbeddingCache(): EmbeddingCache? {
    val url = platform.Foundation.NSFileManager.defaultManager.URLsForDirectory(
        directory = platform.Foundation.NSCachesDirectory,
        inDomains = platform.Foundation.NSUserDomainMask,
    ).firstOrNull() as? platform.Foundation.NSURL ?: return null
    val path = url.path ?: return null
    val root = okio.Path.Companion.run { "$path/confetti-embeddings".toPath() }
    return EmbeddingCache(okio.FileSystem.SYSTEM, root)
}
