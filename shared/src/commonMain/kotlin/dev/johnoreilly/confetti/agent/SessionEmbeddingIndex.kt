@file:OptIn(ExperimentalTime::class)

package dev.johnoreilly.confetti.agent

import ai.koog.embeddings.base.Embedder
import ai.koog.embeddings.base.Vector
import ai.koog.prompt.executor.clients.LLMEmbeddingProviderAPI
import ai.koog.prompt.llm.LLModel
import ai.koog.rag.base.files.DocumentProvider
import ai.koog.rag.base.files.FileSystemProvider
import ai.koog.rag.base.files.readText
import ai.koog.rag.vector.backend.FileVectorStorageBackend
import ai.koog.rag.vector.backend.InMemoryVectorStorageBackend
import ai.koog.rag.vector.backend.VectorStorageBackend
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Path
import kotlin.time.ExperimentalTime

class SessionEmbeddingIndex(
    private val repository: ConfettiRepository,
    private val conference: String,
    private val embedder: Embedder,
    cache: EmbeddingCache?,
) {
    data class Scored(val session: SessionDetails, val score: Double)

    private val fsProvider: OkioFileSystemProvider? = cache?.let { OkioFileSystemProvider(it.fs) }
    private val docProvider: SessionIdDocumentProvider? = fsProvider?.let { SessionIdDocumentProvider(it) }
    private val titleStorage: VectorStorageBackend<String> = backend(cache?.root, "title")
    private val docStorage: VectorStorageBackend<String> = backend(cache?.root, "doc")

    private val mutex = Mutex()
    private var built = false

    private fun backend(root: Path?, subdir: String): VectorStorageBackend<String> =
        if (root == null || fsProvider == null || docProvider == null) {
            InMemoryVectorStorageBackend()
        } else {
            FileVectorStorageBackend(
                documentReader = docProvider,
                fs = fsProvider,
                root = fsProvider.joinPath(root, conference, subdir),
            )
        }

    suspend fun search(query: String, topK: Int): List<Scored> {
        ensureBuilt()
        val queryVector = embedder.embed(query)

        val titleScores = mutableMapOf<String, Double>()
        titleStorage.allDocumentsWithPayload().toList().forEach { (sessionId, v) ->
            titleScores[sessionId] = v.cosineSimilarity(queryVector)
        }
        val merged = titleScores.toMutableMap()
        docStorage.allDocumentsWithPayload().toList().forEach { (sessionId, v) ->
            val s = v.cosineSimilarity(queryVector)
            merged[sessionId] = maxOf(merged[sessionId] ?: Double.NEGATIVE_INFINITY, s)
        }
        if (merged.isEmpty()) return emptyList()

        val sessions = repository.allSessions(conference).associateBy { it.id }
        return merged.entries
            .sortedByDescending { it.value }
            .take(topK.coerceAtLeast(1))
            .mapNotNull { (id, score) -> sessions[id]?.let { Scored(it, score) } }
    }

    private suspend fun ensureBuilt() {
        if (built) return
        mutex.withLock {
            if (built) return
            val sessions = repository.allSessions(conference)
            val cachedIds = titleStorage.allDocuments().toList().toSet()
            for (session in sessions.filter { it.id !in cachedIds }) {
                val titleVec = embedder.embed(session.title)
                titleStorage.store(session.id, titleVec)
                val docText = session.sessionDescription
                    ?.takeIf { it.isNotBlank() }
                    ?.let { "${session.title}\n$it" }
                    ?: session.title
                val docVec = embedder.embed(docText)
                docStorage.store(session.id, docVec)
            }
            built = true
        }
    }
}

class ApiEmbedder(
    private val provider: LLMEmbeddingProviderAPI,
    private val model: LLModel,
) : Embedder {
    override suspend fun embed(text: String): Vector = Vector(provider.embed(text, model))
    override fun diff(embedding1: Vector, embedding2: Vector): Double =
        1.0 - embedding1.cosineSimilarity(embedding2)
}

private class SessionIdDocumentProvider(
    private val fs: FileSystemProvider.ReadOnly<Path>,
) : DocumentProvider<Path, String> {
    override suspend fun document(path: Path): String? =
        if (fs.exists(path)) fs.readText(path) else null

    override suspend fun text(document: String): CharSequence = document
}

internal suspend fun ConfettiRepository.allSessions(conference: String): List<SessionDetails> {
    return sessionsQuery(conference, FetchPolicy.CacheFirst)
        .execute()
        .data
        ?.sessions
        ?.nodes
        ?.map { it.sessionDetails }
        .orEmpty()
}
