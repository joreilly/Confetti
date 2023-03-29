package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import net.mbonnin.bare.graphql.*
import kotlin.time.Duration.Companion.hours

object Sessionize {
    private val droidConLondon2022 = "https://sessionize.com/api/v2/qi0g29hw/view/All"
    private val kotlinConf2023 = "https://sessionize.com/api/v2/rje6khfn/view/All"
    private val androidMakers2023 = "https://sessionize.com/api/v2/72i2tw4v/view/All"

    data class SessionizeData(
        val rooms: List<DRoom>,
        val sessions: List<DSession>,
        val speakers: List<DSpeaker>,
    )

    suspend fun importDroidConLondon2022(): Int {
        return writeData(
            getData(droidConLondon2022),
            config = DConfig(
                id = ConferenceId.DroidConLondon2022.id,
                name = "droidcon London",
                timeZone = "Europe/London"
            ),
            venue = DVenue(
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
    }

    suspend fun importKotlinConf2023(): Int {
        return writeData(
            getData(kotlinConf2023).let {
                it.copy(sessions = it.sessions.filter { it.start.date.dayOfMonth != 12 })
            },
            config = DConfig(
                id = ConferenceId.KotlinConf2023.id,
                name = "KotlinConf 2023",
                timeZone = "Europe/Amsterdam"
            ),
            venue = DVenue(
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
    }

    suspend fun importAndroidMakers2023(): Int {
        return writeData(
            getData(androidMakers2023),
            config = DConfig(
                id = ConferenceId.AndroidMakers2023.id,
                name = "Android Makers 2023",
                timeZone = "Europe/Paris",
                days = listOf(LocalDate(2023, 4, 27), LocalDate(2023, 4, 28))
            ),
            venue = DVenue(
                id = "main",
                name = "Beffroi de Montrouge",
                address = "Av. de la République, 92120 Montrouge",
                description = mapOf(
                    "en" to "Cool venue",
                    "fr" to "Venue fraiche",
                ),
                latitude = null,
                longitude = null,
                imageUrl = null,
                floorPlanUrl = null
            ),
            partnerGroups = partnerGroups("https://raw.githubusercontent.com/paug/AndroidMakersApp/ce800d6eefa4f83d34690161637d7f98918ee4a3/data/sponsors.json")
        )
    }

    private fun writeData(
        sessionizeData: SessionizeData,
        config: DConfig,
        venue: DVenue,
        partnerGroups: List<DPartnerGroup> = emptyList()
    ): Int {
        return DataStore().write(
            sessions = sessionizeData.sessions.sortedBy { it.start },
            rooms = sessionizeData.rooms,
            speakers = sessionizeData.speakers,
            partnerGroups = partnerGroups,
            config = config,
            venues = listOf(venue)
        )
    }

    private suspend fun getData(url: String): SessionizeData {
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

        var rooms = data.asMap["rooms"].asList.map { it.asMap }.map {
            DRoom(
                id = it.get("id").toString(),
                name = it.get("name").asString
            )
        }
        if (rooms.isEmpty()) {
            rooms = sessions.flatMap { it.rooms }.distinct().map { DRoom(id = it, name = it) }
        }
        val speakers = data.asMap["speakers"].asList.map { it.asMap }.map {
            DSpeaker(
                id = it.get("id").asString,
                name = it.get("fullName").asString,
                photoUrl = it.get("profilePicture")?.asString,
                bio = it.get("bio")?.asString,
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

        return SessionizeData(
            rooms = rooms,
            sessions = sessions,
            speakers = speakers
        )
    }
}