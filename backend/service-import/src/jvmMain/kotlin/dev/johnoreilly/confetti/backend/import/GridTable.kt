import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.import.Sessionize
import dev.johnoreilly.confetti.backend.import.getUrl
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

object GridTable {
    suspend fun getData(url: String): Sessionize.SessionizeData {
        val jsonString = getUrl(url)

        val days = Json {
            ignoreUnknownKeys = true
        }.decodeFromString(ListSerializer(Day.serializer()),jsonString)

        val sessions = mutableListOf<DSession>()
        val speakers = mutableListOf<DSpeaker>()
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

                    session.speakers.forEach { speaker ->
                        speakers.add(
                            DSpeaker(
                                id = speaker.id,
                                name = speaker.name,
                                bio = null,
                                tagline = null,
                                company = null,
                                companyLogoUrl = null,
                                city = null,
                                links = emptyList(),
                                photoUrl = null,
                                sessions = listOf(session.id)
                            )
                        )
                    }
                }
            }
        }

        val actualSpeakers = speakers.groupBy { it.id }.values
            .map {
                it.first().copy(
                    sessions = it.map { it.id }
                )
            }

        return Sessionize.SessionizeData(
            rooms = rooms,
            sessions = sessions,
            speakers = actualSpeakers
        )
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
    val speakers: List<Speaker>,
    val categories: JsonArray,
    val roomId: Long,
    val room: String,
)

@Serializable
data class Speaker(
    val id: String,
    val name: String,
)
