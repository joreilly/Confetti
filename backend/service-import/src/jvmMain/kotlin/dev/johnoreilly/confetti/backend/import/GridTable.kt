import dev.johnoreilly.confetti.backend.datastore.DLink
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.import.Sessionize
import dev.johnoreilly.confetti.backend.import.getUrl
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

object GridTable {
    suspend fun getData(url: String): Sessionize.SessionizeData {
        val jsonString = getUrl(url)

        val days = Json {
            ignoreUnknownKeys = true
        }.decodeFromString(ListSerializer(Day.serializer()),jsonString)

        val sessions = mutableListOf<DSession>()
        val rooms = mutableListOf<DRoom>()

        days.forEach { day ->
            day.rooms.forEach { room ->
                if (rooms.none { it.id == room.id.toString() }) {
                    rooms.add(
                        DRoom(
                            id = room.id.toString(),
                            name = room.name
                        )
                    )
                }

                room.sessions.forEach {session ->
                    sessions.add(
                        DSession(
                            id = session.id,
                            title = session.title,
                            start = LocalDateTime.parse(session.startsAt),
                            end = LocalDateTime.parse(session.endsAt),
                            type = if (session.isServiceSession) "service" else "talk",
                            description = session.description,
                            shortDescription = session.description,
                            language = "en-US",
                            complexity = null,
                            feedbackId = null,
                            tags = emptyList(),
                            rooms = listOf(room.id.toString()),
                            speakers = session.speakers.map { it.id },
                            links = emptyList()
                        )
                    )
                }
            }
        }

        return Sessionize.SessionizeData(
            rooms = rooms,
            sessions = sessions,
            speakers = getSpeakers()
        )
    }

    private suspend fun getSpeakers(): List<DSpeaker> {
        val jsonString = getUrl("https://sessionize.com/api/v2/eewr8kdk/view/speakers")
        val speakers = Json {
            ignoreUnknownKeys = true
        }.decodeFromString(ListSerializer(Speaker.serializer()),jsonString)

        return speakers.map {
            DSpeaker(
                id = it.id,
                name = it.fullName,
                bio = it.bio,
                tagline = it.tagLine,
                company = null,
                companyLogoUrl = null,
                city = null,
                links = it.links.map {
                    DLink(
                        key = it.linkType,
                        url = it.url
                    )
                },
                photoUrl = it.profilePicture,
                sessions = it.sessions.map { it.id.toString() }
            )
        }

    }
}

@Serializable
data class Day(
    val date: String,
    val rooms: List<Room>,
)

@Serializable
data class Room(
    val id: Long,
    val name: String,
    val sessions: List<Session>,
)

@Serializable
data class Session(
    val id: String,
    val title: String,
    val description: String? = null,
    val startsAt: String,
    val endsAt: String,
    val isServiceSession: Boolean,
    val isPlenumSession: Boolean,
    val speakers: List<SessionSpeaker>,
    val roomId: Long,
    val room: String,
)

@Serializable
data class SessionSpeaker(
    val id: String,
    val name: String,
)


@Serializable
data class Speaker(
    val id: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val bio: String?,
    val tagLine: String?,
    val profilePicture: String?,
    val sessions: List<SpeakerSession>,
    val isTopSpeaker: Boolean,
    val links: List<Link>,
)

@Serializable
data class Link(
    val title: String,
    val url: String,
    val linkType: String
)

@Serializable
data class SpeakerSession(
    val id: Long,
    val name: String
)