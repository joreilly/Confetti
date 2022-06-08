package fr.androidmakers.server

import fr.androidmakers.server.model.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

@OptIn(ExperimentalSerializationApi::class)
class DevFestLilleDataSource : DataSource {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    companion object {
        private const val MAX_AGE = 5 * 60 * 100
    }

    private val client = OkHttpClient()

    private class Resource<T : Any>(val millis: Long, val data: T)

    private val resourceCache = mutableMapOf<String, Resource<*>>()
    private fun <T : Any> getResource(resourceName: String, block: (InputStream) -> T): T {
        val resource = resourceCache.get(resourceName)

        if (resource == null || System.currentTimeMillis() - resource.millis > MAX_AGE) {
            val url = "https://cms4partners-ce427.nw.r.appspot.com/events/2022/agenda"
            val data = client.newCall(Request.Builder().url(url).build())
                .execute()
                .body!!
                .byteStream()
                .use {
                    block(it)
                }
            resourceCache.put(resourceName, Resource(System.currentTimeMillis(), data))
        }

        return resourceCache.get(resourceName)?.data as? T ?: error("Error getting $resourceName")
    }

    override fun rooms(): List<Room> {
        return allSessions().map { it.roomId }.distinct().map {
            Room(id = it, name = it, capacity = null)
        }
    }

    override fun venue(id: String): Venue {
        TODO()
    }

    private fun getData(): JsonAgenda {
        return getResource("https://cms4partners-ce427.nw.r.appspot.com/events/2022/agenda") {
            json.decodeFromStream<JsonAgenda>(it)
        }
    }

    override fun allSessions(): List<Session> {
        return getData().talks.flatMap {
            it.value.mapNotNull { talkWrapper ->
                if (talkWrapper.talk == null) {
                    return@mapNotNull null
                }
                Session(
                    id = talkWrapper.id,
                    title = talkWrapper.talk.title,
                    description = talkWrapper.talk.abstract,
                    language = "fr-FR",
                    speakerIds = talkWrapper.talk.speakers.map { it.id }.toSet(),
                    tags = emptyList(),
                    roomId = talkWrapper.room,
                    startInstant = LocalDateTime.parse(talkWrapper.startTime).toInstant(TimeZone.of("Europe/Paris")),
                    endInstant = LocalDateTime.parse(talkWrapper.endTime).toInstant(TimeZone.of("Europe/Paris")),
                )
            }
        }

    }

    override fun sessions(first: Int, after: String?): SessionConnection {
        return sliceSessions(allSessions(), first, after)
    }

    override fun speakers(): List<Speaker> {
        return getData().talks.flatMap {
            it.value.flatMap {
                it.talk?.speakers.orEmpty()
            }
        }.map {
            Speaker(
                id = it.id,
                name = it.display_name,
                bio = it.bio,
                photoUrl = it.photo_url,
                company = it.company,
                socials = emptyList()
            )
        }.distinct()
    }

    override fun partners(): List<PartnerGroup> {
        return emptyList()
    }
}
