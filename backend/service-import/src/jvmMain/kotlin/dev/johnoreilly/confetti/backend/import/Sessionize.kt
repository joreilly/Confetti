package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.LocalDateTime
import net.mbonnin.bare.graphql.*

object Sessionize {
    private val droidConLondon2022 = "https://sessionize.com/api/v2/qi0g29hw/view/All"

    fun importDroidConLondon2022() {
        import("droidconlondon2022", droidConLondon2022)
    }
    private fun import(conf: String, url: String) {
        val data = getJsonUrl(url)

        val sessions = data.asMap["sessions"].asList.map {
            it.asMap
        }.map {
            DSession(
                id = it.get("id").asString,
                type = if (it.get("isServiceSession").cast()) "service" else "talk",
                title = it.get("title").asString,
                description = it.get("description")?.asString,
                language = "en-US",
                start = it.get("startsAt").asString.let { LocalDateTime.parse(it) },
                end = it.get("endsAt").asString.let { LocalDateTime.parse(it) },
                complexity = null,
                feedbackId = null,
                tags = emptyList(),
                rooms = listOf(it.get("roomId").toString()),
                speakers = it.get("speakers").asList.map { it.asString }
            )
        }

        val rooms = data.asMap["rooms"].asList.map { it.asMap }.map {
            DRoom(
                id = it.get("id").toString(),
                name = it.get("name").asString
            )
        }
        val speakers = data.asMap["speakers"].asList.map { it.asMap }.map {
            DSpeaker(
                id = it.get("id").asString,
                name = it.get("fullName").asString,
                photoUrl = it.get("profilePicture").asString,
                bio = it.get("bio").asString,
                city = null,
                company = null,
                companyLogoUrl = null,
                links = it.get("links").asList.map { it.asMap }.map {
                    DLink(
                        key = it.get("linkType").asString,
                        url = it.get("url").asString
                    )
                }
            )
        }
        DataStore().write(
            conf = conf,
            sessions = sessions.sortedBy { it.start },
            rooms = rooms,
            speakers = speakers,
            partnerGroups = emptyList(),
            config = DConfig(
                timeZone = "Europe/London"
            ),
            venues = listOf(
                DVenue(
                    id = "main",
                    name = "Business Design Center",
                    address = "52 Upper St, London N1 0QH, United Kingdom",
                    description = mapOf(
                        "en" to "Cool venue",
                        "fr" to "Venue fraiche",
                    ),
                    latitude = 51.5342463,
                    longitude = -0.1068864,
                    imageUrl = "https://london.droidcon.com/wp-content/uploads/sites/3/2022/07/Venue2-1.png"
                )
            )
        )
    }
}