package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.asList
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.asString
import net.mbonnin.bare.graphql.toAny
import okhttp3.OkHttpClient
import okhttp3.Request

object FrenchKit {


    private fun String.toRoom() : String{
        return if (this.isBlank()) {
            "all"
        } else {
            this
        }
    }

    fun import() {
        val schedule = getJsonUrl("https://frenchkit.fr/schedule/schedule-14.json")
        val speakersJson = getJsonUrl("https://frenchkit.fr/speakers/speakers-8.json")

        val sessions = schedule.asList.map {
            it.asMap
        }.map {
            DSession(
                id = it.get("id").asString,
                type = it.get("type").asString,
                title = it.get("title").asString,
                description = it.get("summary")?.asString,
                language = "en-US",
                start = it.get("fromTime").asString.replace(" ", "T").let { LocalDateTime.parse(it) },
                end = it.get("toTime").asString.replace(" ", "T").let { LocalDateTime.parse(it) },
                complexity = null,
                feedbackId = null,
                tags = emptyList(),
                rooms = listOf(it.get("room").asString.toRoom()),
                speakers = it.get("speakers").asList.map { it.asMap.get("id").asString }
            )
        }

        val rooms = sessions.flatMap { it.rooms }.map { DRoom(it, it) }
        val speakers = speakersJson.asList.map { it.asMap }.map {
            DSpeaker(
                id = it.get("id").asString,
                name = it.get("firstName").asString,
                photoUrl = it.get("imageURL").asString,
                bio = null,
                city = null,
                company = null,
                companyLogoUrl = null,
                links = emptyList()
            )
        }
        DataStore().write(
            conf = "frenchkit2022",
            sessions = sessions.sortedBy { it.start },
            rooms = rooms,
            speakers = speakers,
            partnerGroups = emptyList(),
            config = DConfig(
                name = "FrenchKit",
                timeZone = "Europe/Paris"
            ),
            venues = listOf(
                DVenue(
                    id = "main",
                    name = "Pan Piper",
                    address = "En face du père lachaise",
                    description = mapOf(
                        "en" to "Cool venue",
                        "fr" to "Venue fraiche",
                    ),
                    latitude = null,
                    longitude = null,
                    imageUrl = null
                )
            )
        )
    }
}