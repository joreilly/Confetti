package dev.johnoreilly.confetti.backend.resize

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.ScaleMethod
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter
import dev.johnoreilly.confetti.backend.graphql.DataStoreDataSource
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.buildAndAwait
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URI

/**
 * Utility to fetch and resize avatar images to optimise for
 * mobile or wear.
 */
class AvatarFetcher(
    private val client: WebClient,
) {
    val format: ImageWriter = PngWriter()
    val bufferFactory: DataBufferFactory = DefaultDataBufferFactory()

    suspend fun resize(conference: String, speakerId: String, size: AvatarSize): ServerResponse {
        val dataSource = DataStoreDataSource(conference)

        val speaker = dataSource.speaker(speakerId)
        val url = speaker.photoUrl ?: return notFound().buildAndAwait()

        return client.get().uri(URI.create(url)).exchangeToMono { response ->
            val statusCode = response.statusCode()
            if (statusCode.is4xxClientError || statusCode.is5xxServerError) {
                return@exchangeToMono status(statusCode).build()
            }

            DataBufferUtils.join(response.body(BodyExtractors.toDataBuffers())).map { dataBuffer ->
                val original = ImmutableImage.loader().fromStream(dataBuffer.asInputStream())

                if (original.height > size.size) {
                    original.scaleToHeight(size.size, ScaleMethod.Progressive, true)
                } else {
                    original
                }
            }
                .map { bufferFactory.wrap(it.bytes(format)) }
                .flatMap { dataBuffer ->
                    ok()
                        .contentType(MediaType.parseMediaType("image/png"))
                        .header("Cache-Control", "public, max-age=1800")
                        .body(Mono.just(dataBuffer), DataBuffer::class.java)
                }
                .subscribeOn(Schedulers.boundedElastic())
        }.awaitSingle()
    }
}

data class AvatarSize(val size: Int) {
    companion object {
        fun fromParam(param: String?): AvatarSize = when (param) {
            "Watch" -> AvatarSize(96)
            "Phone" -> AvatarSize(256)
            else -> AvatarSize(256)
        }
    }
}