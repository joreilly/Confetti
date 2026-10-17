package dev.johnoreilly.confetti.agent

import ai.koog.embeddings.base.Vector
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.EmbeddingEngine
import com.google.ai.edge.litertlm.EmbeddingEngineConfig
import com.google.ai.edge.litertlm.EmbeddingOptions
import com.google.ai.edge.litertlm.InputData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * On-device [RetrievalEmbedder] running EmbeddingGemma 2 (text, 270M) via LiteRT-LM.
 *
 * The model isn't bundled (~165 MB); download it from
 * https://huggingface.co/litert-community/embeddinggemma-2-text-270m-litert-lm and push it:
 *
 * adb push embeddinggemma-2-text-270m.litertlm \
 *   /sdcard/Android/data/dev.johnoreilly.confetti/files/
 *
 * On a Tensor G5 device, push `embeddinggemma-2-text-270m_Google_Tensor_G5.litertlm` instead
 * to run on the NPU.
 */
class LiteRtEmbedder(
    val modelFile: File,
    private val backend: Backend,
    private val cacheDir: File,
) : RetrievalEmbedder, OnDeviceEmbedder {

    private val mutex = Mutex()
    private var engine: EmbeddingEngine? = null

    // Task prompts from EmbeddingGemma 2's sentence-transformers config.
    override suspend fun embedQuery(text: String): Vector = compute("task: search result | query: $text")

    override suspend fun embedDocument(text: String): Vector = compute("title: none | text: $text")

    override suspend fun embed(text: String): Vector = embedDocument(text)

    override fun diff(embedding1: Vector, embedding2: Vector): Double =
        1.0 - embedding1.cosineSimilarity(embedding2)

    private suspend fun compute(text: String): Vector = withContext(Dispatchers.IO) {
        mutex.withLock {
            val response = engine().computeEmbeddingAsync(
                listOf(InputData.Text(text)),
                EmbeddingOptions(normalize = true),
            )
            Vector(response.embedding.map { it.toDouble() })
        }
    }

    private fun engine(): EmbeddingEngine = engine ?: EmbeddingEngine(
        EmbeddingEngineConfig(
            modelPath = modelFile.absolutePath,
            backend = backend,
            cacheDir = cacheDir.absolutePath,
        )
    ).also {
        it.initialize()
        engine = it
    }

    companion object {
        const val MODEL_FILE_NAME = "embeddinggemma-2-text-270m.litertlm"
        const val TENSOR_G5_MODEL_FILE_NAME = "embeddinggemma-2-text-270m_Google_Tensor_G5.litertlm"
    }
}
