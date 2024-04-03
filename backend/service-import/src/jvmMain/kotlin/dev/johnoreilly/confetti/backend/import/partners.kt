@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSerializationApi::class)

package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.DPartner
import dev.johnoreilly.confetti.backend.datastore.DPartnerGroup
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
class JsonPartnerGroup(
    val kind: String,
    val items: List<JsonPartner>
)

@Serializable
class JsonPartner(
    val name: String,
    val url: String,
    val photoUrl: String
)


fun partnerGroups(url: String): List<DPartnerGroup> {
    val client = OkHttpClient.Builder()
        .build()

    return Request.Builder()
        .get()
        .url(url)
        .build().let {
            client.newCall(it).execute()
        }.also {
            check(it.isSuccessful) {
                "Error executing $url"
            }
        }
        .body.byteStream().use {
            Json {
                ignoreUnknownKeys = true
            }.decodeFromStream(ListSerializer(JsonPartnerGroup.serializer()), it)
        }.let {
            it.map { it.toDPartnerGroup() }
        }
}

private fun JsonPartnerGroup.toDPartnerGroup(): DPartnerGroup {
    return DPartnerGroup(
        key = kind,
        partners = items.map { it.toDPartner() }
    )
}

private fun JsonPartner.toDPartner(): DPartner {
    return DPartner(
        name = name,
        logoUrl = photoUrl,
        logoUrlDark = null,
        url = url
    )
}