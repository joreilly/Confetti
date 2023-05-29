package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DVenue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object TechConnection {
    private val venue = DVenue(
        id = "panpiper",
        name = "Pan Piper",
        address = "2-4 Imp. Lamier, 75011 Paris",
        latitude = 48.8578509,
        longitude = 2.3889876,
        description = emptyMap(),
        imageUrl = "https://pan-piper.com/wp-content/uploads/2021/09/pan_piper_auditorium_conference.jpg",
        floorPlanUrl = null
    )

    fun importFlutter2023(): Int {
        return import(
            url = "https://flutterconnection.io/api/v1/schedule",
            config = DConfig(
                id = ConferenceId.FlutterConnection2023.id,
                name = "Flutter Connection",
                timeZone = "Europe/Paris",
                days = listOf(LocalDate(year = 2023, monthNumber = 6, dayOfMonth = 2))
            ),
            venue = venue
        )
    }

    fun importReactNative2023(): Int {
        return import(
            url = "https://reactnativeconnection.io/api/v1/schedule",
            config = DConfig(
                id = ConferenceId.ReactNativeConnection2023.id,
                name = "React Native Connection",
                timeZone = "Europe/Paris",
                days = listOf(LocalDate(year = 2023, monthNumber = 6, dayOfMonth = 1))
            ),
            venue = venue
        )
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private fun import(config: DConfig, venue: DVenue, url: String) = runBlocking {
        val talks = json.decodeFromString<List<TCTalk>>(getUrl(url))

        val speakersToTalks = talks.flatMap { talk ->
            talk.speakersPlainText.orEmpty().split(",").map { it.trim() to talk }
        }.groupBy( { it.first }, {it.second})
            .filter { it.key.isNotBlank() }

        println("speakers: ${speakersToTalks.keys}")

        DataStore().write(
            sessions = talks.map {
                DSession(
                    id = it.id,
                    type = it.kind.toType(),
                    title = it.title,
                    description = it.abstract,
                    shortDescription = it.abstract,
                    language = null,
                    start = Instant.parse(it.fromTime).toLocalDateTime(TimeZone.of(config.timeZone)),
                    end = Instant.parse(it.toTime).toLocalDateTime(TimeZone.of(config.timeZone)),
                    complexity = null,
                    feedbackId = null,
                    tags = emptyList(),
                    rooms = listOf("Main Track"),
                    speakers = emptyList(),
                    links = emptyList(),
                )
            },
            rooms = listOf(DRoom("Main Track", "Main Track")),
            speakers = speakersToTalks
                .map {
                    DSpeaker(
                        id = it.key,
                        name = it.key,
                        bio = null,
                        tagline = null,
                        company = null,
                        companyLogoUrl = null,
                        city = null,
                        links = emptyList(),
                        photoUrl = null,
                        sessions = it.value.map { it.id }
                    )
                },
            partnerGroups = emptyList(),
            config = config,
            venues = listOf(venue)
        )
        talks.size
    }
}

private fun String?.toType(): String {
    return this?.lowercase() ?: "talk"
}

@Serializable
private class TCTalk(
    val id: String,
    val title: String,
    val speakersPlainText: String?,
    val fromTime: String,
    val toTime: String,
    val kind: String?,
    val abstract: String?,
)