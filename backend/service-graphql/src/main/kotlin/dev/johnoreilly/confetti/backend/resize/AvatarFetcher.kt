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
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.buildAndAwait
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

        return client.get().uri(URI.create(url)).awaitExchange {
            if (it.statusCode().is4xxClientError || it.statusCode().is5xxServerError) {
                return@awaitExchange status(it.statusCode()).buildAndAwait()
            }

            // TODO check for a better safer way to wire this up...
            val outputStream = PipedOutputStream()
            val inputStream = PipedInputStream(1024 * 10)
            inputStream.connect(outputStream)

            val body = it.body(BodyExtractors.toDataBuffers())
            // TODO check for a better way to close
            DataBufferUtils.write(body, outputStream)
                .doOnComplete { outputStream.close() }
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(DataBufferUtils.releaseConsumer())

            // TODO find way to work only in coroutines land, not Reactor
            val buffer = Mono.fromCallable {
                inputStream.use {
                    val original = ImmutableImage.loader().fromStream(inputStream)

//                    if (original.height > size.size) {
                        original.scaleToHeight(size.size, ScaleMethod.Progressive, true)
//                    } else {
//                        original
//                    }
                }
            }
                .map { bufferFactory.wrap(it.bytes(format)) }
                .subscribeOn(Schedulers.boundedElastic())

            ok()
                .contentType(MediaType.parseMediaType("image/png"))
                .body(buffer, DataBuffer::class.java)
                .awaitSingle()
        }
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