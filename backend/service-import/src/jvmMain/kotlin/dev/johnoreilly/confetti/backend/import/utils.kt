package dev.johnoreilly.confetti.backend.import

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.toAny
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.executeAsync

private val okHttpClient = OkHttpClient.Builder()
    .fastFallback(true)
    .build()

suspend fun getUrl(url: String): String {
    val request = Request(url.toHttpUrl())

    val response = okHttpClient.newCall(request).executeAsync().also {
        check(it.isSuccessful) {
            "Cannot get $url: ${it.body.string()}"
        }
    }

    return withContext(Dispatchers.IO) { response.body.string() }
}

suspend fun getJsonUrl(url: String) = Json.parseToJsonElement(getUrl(url)).toAny()