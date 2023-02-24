package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.LocalDateTime
import net.mbonnin.bare.graphql.*

object Sessionize {
    private val droidConLondon2022 = "https://sessionize.com/api/v2/qi0g29hw/view/All"
    private val kotlinConf2023 = "https://sessionize.com/api/v2/rje6khfn/view/All"

    suspend fun importDroidConLondon2022() {
        import(ConferenceId.DroidConLondon2022.id, "droidcon London", droidConLondon2022)
    }

    suspend fun importKotlinConf2023() {
        import(ConferenceId.KotlinConf2023.id, "KotlinConf 2023", kotlinConf2023)
    }
    private suspend fun import(conf: String, confName: String, url: String) {
        val data = getJsonUrl(url)

        val categories = data.asMap["categories"].asList.map { it.asMap }
            .flatMap {
                it["items"].asList
            }.map {
                it.asMap
            }.map {
                it["id"] to it["name"]
            }.toMap()
        val sessions = data.asMap["sessions"].asList.map {
            it.asMap
        }.mapNotNull {
            if (it.get("startsAt") == null || it.get("endsAt") == null){
                /**
                 * Guard against sessions that are not scheduled.
                 */
                return@mapNotNull null
            }
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
                tags = it.get("categoryItems").asList.mapNotNull { categoryId ->
                    categories.get(categoryId)?.asString
                },
                rooms = listOf(it.get("roomId").toString()),
                speakers = it.get("speakers").asList.map { it.asString },
                shortDescription = null,
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
            sessions = sessions.sortedBy { it.start },
            rooms = rooms,
            speakers = speakers,
            partnerGroups = emptyList(),
            config = DConfig(
                id = conf,
                name = confName,
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
                    imageUrl = "https://london.droidcon.com/wp-content/uploads/sites/3/2022/07/Venue2-1.png",
                    floorPlanUrl = null
                )
            )
        )
    }
}