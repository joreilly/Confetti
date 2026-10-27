package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The agenda at https://rsvp.withgoogle.com/events/android-dev-summit-london/agenda is only visible
 * once signed in to a Google account, so it's transcribed into a bundled JSON resource.
 */
object AndroidDevSummitLondon2026 {
    private const val FILE = "androiddevsummitlondon2026.json"
    private val day = LocalDate(2026, 10, 28)

    @Serializable
    private class Agenda(val speakers: List<Speaker>, val sessions: List<Session>)

    @Serializable
    private class Speaker(
        val id: String,
        val name: String,
        val tagline: String?,
        val company: String?,
        val photoUrl: String?,
    )

    @Serializable
    private class Session(
        val start: String,
        val end: String,
        val room: String,
        val type: String,
        val title: String,
        val description: String,
        val speakers: List<String>,
    )

    private val config = DConfig(
        id = ConferenceId.AndroidDevSummitLondon2026.id,
        name = "Android Dev Summit Extended London 2026",
        timeZone = "Europe/London",
        days = listOf(day),
        themeColor = "0xFF3DDC84"
    )

    private val venue = DVenue(
        id = "main",
        name = "Google, 6 Pancras Square",
        address = "6 Pancras Square, London N1C 4AG",
        latitude = null,
        longitude = null,
        description = emptyMap(),
        imageUrl = null,
        floorPlanUrl = null
    )

    suspend fun import(): Int {
        val text = javaClass.classLoader.getResourceAsStream(FILE)!!.use { it.reader().readText() }
        val agenda = Json.decodeFromString<Agenda>(text)

        val sessions = agenda.sessions.mapIndexed { index, s ->
            DSession(
                id = index.toString(),
                type = s.type,
                title = s.title,
                description = s.description,
                shortDescription = null,
                language = "en-US",
                start = LocalDateTime(day, LocalTime.parse(s.start)),
                end = LocalDateTime(day, LocalTime.parse(s.end)),
                complexity = null,
                feedbackId = null,
                tags = emptyList(),
                rooms = listOf(s.room),
                speakers = s.speakers,
                links = emptyList()
            )
        }

        val speakerSessions = sessions
            .flatMap { session -> session.speakers.map { it to session.id } }
            .groupBy({ it.first }, { it.second })

        val knownSpeakers = agenda.speakers.map { it.id }.toSet()
        sessions.flatMap { it.speakers }.filter { it !in knownSpeakers }.let {
            check(it.isEmpty()) { "Unknown speakers: $it" }
        }

        DataStore().write(
            sessions = sessions,
            rooms = sessions.flatMap { it.rooms }.distinct().map { DRoom(it, it) },
            speakers = agenda.speakers.map {
                DSpeaker(
                    id = it.id,
                    name = it.name,
                    bio = null,
                    tagline = it.tagline,
                    company = it.company,
                    companyLogoUrl = null,
                    city = null,
                    links = emptyList(),
                    photoUrl = it.photoUrl,
                    sessions = speakerSessions[it.id]
                )
            },
            partnerGroups = emptyList(),
            config = config,
            venues = listOf(venue)
        )

        return sessions.size
    }
}
