package dev.johnoreilly.confetti.images

import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.sun.net.httpserver.HttpServer
import dev.johnoreilly.confetti.di.KoinTestApp
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress

/**
 * Speaker photos in the shared Compose screens are coil3 `AsyncImage`s on Coil's default
 * singleton, whose coil-network-ktor3 fetcher uses `HttpClient()`: the engine is whichever one the
 * app's classpath offers. That must be OkHttp, as on main, and only OkHttp. A second engine
 * (rc-player-compose 2.22 declared CIO) makes the choice arbitrary.
 */
@RunWith(RobolectricTestRunner::class)
// SDK 27: below 28 Coil decodes with BitmapFactory, which Robolectric runs; its ImageDecoder path
// (28+) throws "Only supported on Android" here, so a newer SDK could check the fetch but not the image.
@Config(application = KoinTestApp::class, sdk = [27])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpeakerPhotoLoadingTest {
    @After fun stopKoinAfterTest() = stopKoin()

    @Test fun defaultKtorEngineIsOkHttp() {
        HttpClient().use { assertEquals("OkHttpEngine", it.engine::class.simpleName) }
    }

    @Test fun coilSingletonLoadsARemotePhoto() {
        val png = ByteArrayOutputStream().also {
            Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, it)
        }.toByteArray()
        val hits = java.util.concurrent.atomic.AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/image/speaker.png") { exchange ->
                hits.incrementAndGet()
                exchange.responseHeaders.add("Content-Type", "image/png")
                exchange.sendResponseHeaders(200, png.size.toLong())
                exchange.responseBody.use { it.write(png) }
            }
            start()
        }
        try {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val url = "http://127.0.0.1:${server.address.port}/image/speaker.png"
            val result = runBlocking { SingletonImageLoader.get(context).execute(ImageRequest.Builder(context).data(url).build()) }
            assertEquals(1, hits.get())
            assertTrue("$result ${(result as? ErrorResult)?.throwable}", result is SuccessResult)
            assertEquals(4, (result as SuccessResult).image.width)
        } finally {
            server.stop(0)
        }
    }
}
