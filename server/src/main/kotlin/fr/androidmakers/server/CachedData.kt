package fr.androidmakers.server

import fr.androidmakers.server.model.*
import kotlinx.datetime.toInstant
import okio.buffer
import okio.source
import xoxo.firstNonBlankTextContent
import xoxo.toXmlDocument
import xoxo.walkElements
import java.util.*


private class SessionizeItem(
    val title: String,
    val room: String,
    val start: String,
    val end: String,
    val language: String,
    val speakers: List<SessionizeSpeaker>,
)

private class SessionizeSpeaker(
    val id: String,
    val name: String,
)

object CachedData {
    private val data: List<SessionizeItem> by lazy {
        javaClass.classLoader!!.getResourceAsStream("sessionize.xml").source().buffer()
            .toXmlDocument()
            .root // <div class="sz-root">
            .walkElements()
            .filter {
                it.attributes.containsKey("data-sessionid")
            }
            .map {
                val room =
                    it.walkElements().first { it.attributes["class"] == "sz-session__room" }
                        .firstNonBlankTextContent()
                // "TimeWithDuration|en-US|2022-06-02T15:00:00.0000000Z|2022-06-02T16:30:00.0000000Z"
                val sztz = it.walkElements().mapNotNull { it.attributes["data-sztz"] }.first()
                val parts = sztz.split("|")
                val language = parts[1]
                val start = parts[2]
                val end = parts[3]
                val title =
                    it.walkElements().first { it.attributes["class"] == "sz-session__title" }
                        .firstNonBlankTextContent()
                        .replace("\n", "")
                        .replace(Regex("  *"), " ")

                val speakers =
                    it.walkElements().filter { it.attributes.containsKey("data-speakerid") }.map {
                        val name = it.firstNonBlankTextContent()
                        SessionizeSpeaker(id = name, name = name)
                    }.toList()

                SessionizeItem(
                    title = title,
                    room = room,
                    language = language,
                    start = start,
                    end = end,
                    speakers = speakers
                )
            }.toList()
    }

    // TODO This must not be global but per user of course! And also, persisted.
    // Need to implement some kind of authetication
    private val favoriteSessionIds = mutableListOf<String>()

    fun rooms(): List<Room> {
        return data.map { it.room }.distinct().map {
            Room(
                id = it,
                name = it,
                capacity = null
            )
        }
    }

    fun venue(id: String): Venue {
        TODO()
    }

    fun allSessions(): List<Session> {
        return data.map {
            val id = it.start + " " + it.title
            Session(
                id = id,
                title = it.title,
                description = it.title,
                language = it.language,
                speakerIds = it.speakers.map { it.id }.toSet(),
                tags = emptyList(),
                startInstant = it.start.toInstant(),
                endInstant = it.end.toInstant(),
                roomId = it.room,
                isFavorite = favoriteSessionIds.contains(id)
            )
        }
    }

    fun setSessionFavorite(sessionId: String, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteSessionIds.add(sessionId)
        } else {
            favoriteSessionIds.remove(sessionId)
        }
    }

    fun sessions(first: Int, after: String?): SessionConnection {
        val sessionList = allSessions()
        val fromIndex = if (after == null) 0 else sessionList.indexOfFirst { it.id == after.decodeBase64() } + 1
        val toIndex = (fromIndex + first).coerceAtMost(sessionList.size)
        val sessionSubList = sessionList.subList(fromIndex = fromIndex, toIndex = toIndex)
        val edges = sessionSubList.map { session -> SessionEdge(node = session, cursor = session.id.encodeBase64()) }
        return SessionConnection(
            totalCount = sessionList.size,
            edges = edges,
            pageInfo = PageInfo(
                hasPreviousPage = fromIndex > 0,
                hasNextPage = toIndex < sessionList.size,
                startCursor = edges.firstOrNull()?.cursor,
                endCursor = edges.lastOrNull()?.cursor,
            )
        )
    }

    fun speakers(): List<Speaker> {
        return data.flatMap {
            it.speakers.map { it.name }
        }.distinct()
            .map {
                Speaker(
                    id = it,
                    name = it,
                    bio = "",
                    photoUrl = null,
                    company = null,
                    socials = emptyList()
                )
            }
    }

    fun partners(): List<PartnerGroup> {
        return emptyList()
    }
}

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(toByteArray())
}

fun String.decodeBase64(): String {
    return String(Base64.getDecoder().decode(this))
}
