package dev.johnoreilly.confetti.backend.import

import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.toAny
import okhttp3.OkHttpClient
import okhttp3.Request

private val okHttpClient = OkHttpClient()

fun getUrl(url: String): String {
    return Request.Builder()
        .url(url)
        .build()
        .let {
            okHttpClient.newCall(it).execute().also {
                check(it.isSuccessful) {
                    "Cannot get $url: ${it.body?.string()}"
                }
            }
        }.body!!.string()
}

fun getJsonUrl(url: String) = Json.parseToJsonElement(getUrl(url)).toAny()