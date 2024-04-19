package dev.johnoreilly.confetti.backend.import

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.kotlinx.resolveOrNull
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DVenue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import java.io.File

object GraphQLSummit2023 {
    fun import(): Int {
        val dSessions = mutableListOf<DSession>()
        val dRooms = mutableListOf<DRoom>()
        val dSpeakers = mutableListOf<DSpeaker>()

        File("/Users/mbonnin/git/summit/jsons").listFiles().orEmpty()
            .filter { it.extension == "json" }
            .forEach { file ->
            val element = Json.parseToJsonElement(file.readText())
            val helloPath = JsonPath.compile("$[0].data.event.sessions.data")

            val sessions = element.resolveOrNull(helloPath)?.jsonArray ?: return@forEach

            sessions.forEach { it ->
                val session = it.jsonObject

                dSessions.add(
                    DSession(
                        id = session["id"]!!.jsonPrimitive.content,
                        type = "talk",
                        title = session["name"]!!.jsonPrimitive.content,
                        description = Jsoup.parse(session["description"]!!.jsonPrimitive.content).text(),
                        shortDescription = null,
                        language = "en-US",
                        start = session["startDateTime"]!!.jsonPrimitive.content.let { Instant.parse(it) }
                            .toLocalDateTime(TimeZone.of("America/Los_Angeles")),
                        end = session["endDateTime"]!!.jsonPrimitive.content.let { Instant.parse(it) }
                            .toLocalDateTime(TimeZone.of("America/Los_Angeles")),
                        complexity = null,
                        feedbackId = null,
                        tags = emptyList(),
                        rooms = listOf(session["sessionLocation"]?.jsonObjectOrNull?.get("id")?.jsonPrimitive?.content ?: "service"),
                        speakers = session["speakers"]!!.jsonArray.map { it.jsonObject["id"]!!.jsonPrimitive.content },
                        links = emptyList()
                    )
                )

                dRooms.add(
                    DRoom(
                        id = session["sessionLocation"]?.jsonObjectOrNull?.get("id")?.jsonPrimitive?.content ?: "service",
                        name = session["sessionLocation"]?.jsonObjectOrNull?.get("locationName")?.jsonPrimitive?.content ?: "Service Room"
                    )
                )

                session["speakers"]!!.jsonArray.map { it.jsonObject }.forEach {
                    dSpeakers.add(
                        DSpeaker(
                            id = it["id"]!!.jsonPrimitive.content,
                            name = "${it["firstName"]!!.jsonPrimitive.content} ${it["lastName"]!!.jsonPrimitive.content}",
                            bio = it["biography"]?.jsonPrimitive?.content,
                            tagline = null,
                            company = it["company"]?.jsonPrimitive?.content,
                            companyLogoUrl = null,
                            photoUrl = it["profilePictureUri"]?.jsonPrimitive?.content,
                            city = null,
                            links = emptyList(),
                            sessions = emptyList()
                        )
                    )
                }
            }
        }

        return DataStore().write(
            sessions = dSessions.sortedBy { it.start },
            rooms = dRooms.distinctBy { it.id },
            speakers = dSpeakers.distinctBy { it.id }.map { dSpeaker ->
                dSpeaker.copy(sessions = dSessions.filter { it.speakers.contains(dSpeaker.id) }.map { it.id })
            },
            partnerGroups = emptyList(),
            config = DConfig(
                id = ConferenceId.GraphQLSummit2023.id,
                name = "GraphQL Summit 2023",
                timeZone = "America/Los_Angeles",
                days = listOf(9, 10, 11, 12).map { LocalDate(2023, 10, it) }
            ),
            venues = listOf(
                DVenue(
                    id = "sheraton",
                    name = "Sheraton San Diego Hotel & Marina",
                    latitude = 32.7264149,
                    longitude = -117.2014124,
                    address = "Sheraton Bay Tower, 1590 Harbor Island Dr, San Diego, CA 92101, United States",
                    description = emptyMap(),
                    imageUrl = "https://cache.marriott.com/content/dam/marriott-renditions/SANSI/sansi-lagoon-pool-shoreline-3790-hor-clsc.jpg?output-quality=70&interpolation=progressive-bilinear&downsize=2880px:*",
                    floorPlanUrl = null
                )
            )
        )
    }
}

private val JsonElement.jsonObjectOrNull
    get() = (this as? JsonObject?)