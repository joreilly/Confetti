package fr.androidmakers.server

import fr.androidmakers.server.model.*
import kotlinx.datetime.toInstant
import okio.buffer
import okio.source
import xoxo.firstNonBlankTextContent
import xoxo.toXmlDocument
import xoxo.walkElements
import java.util.Base64


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

class DroidConSfoDataSource : DataSource {
    private val data by lazy {
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


    override fun rooms(): List<Room> {
        return data.map { it.room }.distinct().map {
            Room(
                id = it,
                name = it,
                capacity = null
            )
        }
    }

    override fun venue(id: String): Venue {
        TODO()
    }

    override fun allSessions(): List<Session> {
        return data.map {
            Session(
                id = it.title,
                title = it.title,
                description = it.title,
                language = it.language,
                speakerIds = it.speakers.map { it.id }.toSet(),
                tags = emptyList(),
                startInstant = it.start.toInstant(),
                endInstant = it.end.toInstant(),
                roomId = it.room
            )
        }
    }

    override fun sessions(first: Int, after: String?): SessionConnection {
        return sliceSessions(allSessions(), first, after)
    }

    override fun speakers(): List<Speaker> {
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

    override fun partners(): List<PartnerGroup> {
        return emptyList()
    }
}

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(toByteArray())
}

fun String.decodeBase64(): String {
    return String(Base64.getDecoder().decode(this))
}
