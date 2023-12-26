package dev.johnoreilly.confetti.backend.resize

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.ScaleMethod
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.PipedInputStream
import java.io.PipedOutputStream
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
//        val dataSource = DataStoreDataSource(conference)
//
//        val speaker = dataSource.speaker(speakerId)
//        val url = speaker.photoUrl ?: return notFound().buildAndAwait()

        val url = "https://www.festival-infolocale.fr/wp-content/uploads/Me%CC%81lissa-Cottin-1024x1024.png"

        return client.get().uri(URI.create(url)).exchangeToMono { response ->
            val statusCode = response.statusCode()
            if (statusCode.is4xxClientError || statusCode.is5xxServerError) {
                return@exchangeToMono status(statusCode).build()
            }

            // TODO check for a better safer way to wire this up...
            val outputStream = PipedOutputStream()
            val inputStream = PipedInputStream(1024 * 10)
            inputStream.connect(outputStream)

            // Start pumping the bytes from the Response
            val webRequest = DataBufferUtils.write(response.body(BodyExtractors.toDataBuffers()), outputStream)
                .doOnComplete { outputStream.close() }
                .last()
                .subscribeOn(Schedulers.boundedElastic())

            val result = Mono.fromCallable {
                inputStream.use {
                    val original = ImmutableImage.loader().fromStream(inputStream)

                    if (original.height > size.size) {
                        original.scaleToHeight(size.size, ScaleMethod.Progressive, true)
                    } else {
                        original
                    }
                }
            }
                .map { bufferFactory.wrap(it.bytes(format)) }
                .flatMap { dataBuffer ->
                    ok()
                        .contentType(MediaType.parseMediaType("image/png"))
                        .body(Mono.just(dataBuffer), DataBuffer::class.java)
                }
                .subscribeOn(Schedulers.boundedElastic())

            webRequest.zipWith(result) { _, x -> x }
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