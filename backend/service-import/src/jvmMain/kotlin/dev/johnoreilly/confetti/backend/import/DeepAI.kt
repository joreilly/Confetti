package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.DEEPAI_API_KEY
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mbonnin.bare.graphql.toJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor

val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()

fun summarizeText(text: String): String? {
    val request = Request.Builder()
        .post(
            MultipartBody.Builder()
                .setType("multipart/form-data".toMediaType())
                .addFormDataPart("text", null, text.toRequestBody("text/plain".toMediaType()))
                .build()
        ).addHeader("api-key", DEEPAI_API_KEY)
        .url("https://api.deepai.org/api/summarization")
        .build()

    val response = client.newCall(request).execute()
    if(!response.isSuccessful) {
        println("Cannot summarize text: ${response.code}: '${response.body.string()}'")
        return null
    }

    return try {
        val responseString = response.body.string()
        val asJson = Json.parseToJsonElement(responseString)

        val summary = asJson.jsonObject.get("output")!!.jsonPrimitive.content

        if (summary.isNullOrBlank()) {
            // The API could not summarize
            text
        } else {
            summary
        }
    } catch(e: Throwable) {
        println("Cannot decode body")
        e.printStackTrace()
        null
    }
}