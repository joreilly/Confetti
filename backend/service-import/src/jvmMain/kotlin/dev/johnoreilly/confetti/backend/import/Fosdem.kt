package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DVenue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import net.mbonnin.bare.graphql.asString
import okhttp3.OkHttpClient
import okhttp3.Request
import xoxo.XmlElement
import xoxo.toXmlDocument
import kotlin.time.Duration.Companion.minutes

object Fosdem {
    private val timeZone = "Europe/Brussels"

    private fun XmlElement.textOf(childName: String): String? {
        return childElements.singleOrNull {
            it.name == childName
        }?.textContent
    }

    internal fun import() {
        val response = Request.Builder()
            .get()
            .url("https://fosdem.org/schedule/xml")
            .build()
            .let {
                OkHttpClient().newCall(it)
            }.execute()

        if (!response.isSuccessful || response.body == null) {
            error("Cannot get fosdem data: ${response.code}")
        }

        val schedule = response.body!!.source().use {
            it.toXmlDocument()
        }

        val speakersMap = mutableMapOf<String, DSpeaker>()
        val sessions = schedule.root.childElements.filter { day -> day.name == "day" }.flatMap { day ->
            val date = LocalDate.parse(day.attributes.get("date")!!.asString)

            day.childElements.filter { room -> room.name == "room" }.flatMap { room ->
                val roomName = room.attributes.get("name").asString

                room.childElements.filter { it.name == "event" }.map { event ->
                    val id = event.attributes.get("id").asString

                    val start = date.atTime(LocalTime.parse(event.textOf("start")!!))
                    val end = start.toInstant(timeZone = TimeZone.of(timeZone)) + event.textOf("duration")!!.let {
                        (it.substring(0, 2).toInt() * 60 + it.substring(3, 5).toInt()).minutes
                    }
                    val persons = event.childElements.singleOrNull {it.name == "persons" }?.childElements ?: emptyList()

                    val speakers = persons.map {
                        DSpeaker(
                            id = it.attributes.get("id").asString,
                            name = it.textContent,
                            bio = null,
                            company = null,
                            links = emptyList(),
                            companyLogoUrl = null,
                            city = null,
                            photoUrl = null,
                        )
                    }

                    speakersMap.putAll(speakers.associateBy { it.id })

                    DSession(
                        id = id,
                        title = event.textOf("title") ?: error("no title for $id"),
                        description = event.textOf("abstract").stripHtml(),
                        language =  "en-US", // language is not set
                        speakers = speakers.map { it.id },
                        tags = listOf(event.textOf("track").asString),
                        start = start,
                        end = end.toLocalDateTime(TimeZone.of(timeZone)),
                        complexity = null,
                        feedbackId = null,
                        rooms = listOf(roomName),
                        type = "talk"
                    )
                }
            }
        }


        val roomIds = sessions.flatMap { it.rooms }.toSet()

        val rooms = roomIds.map {
            DRoom(
                id = it,
                name = it,
            )
        }

        val config = DConfig(
            id = ConferenceId.Fosdem2023.id,
            name = "Fosdem 2023",
            timeZone = timeZone
        )

        return DataStore().write(
            sessions = sessions.sortedBy { it.start },
            rooms = rooms,
            speakers = speakersMap.values.toList(),
            partnerGroups = emptyList(),
            config = config,
            venues = listOf(
                DVenue(
                    id = "main",
                    name = "Université Libre de Bruxelles",
                    address = "Av. Franklin Roosevelt 50, 1050 Bruxelles",
                    description = emptyMap(),
                    latitude = 50.8132068,
                    longitude = 4.3822222,
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/5/50/DSC_4320%C2%A9_Lara_Herbinia.jpg",
                    floorPlanUrl = null
                )
            )
        )
    }

    // See https://github.com/joreilly/Confetti/issues/199
    private fun String?.stripHtml(): String {
        return this?.replace("<p>", "")
            ?.replace("</p>", "\n")
            ?.replace("<a href=\".+\">(.+)</a>".toRegex(), "$1")
            .orEmpty()
    }
}