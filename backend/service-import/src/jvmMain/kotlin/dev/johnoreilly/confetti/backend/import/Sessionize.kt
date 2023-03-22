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


    suspend fun importDroidConLondon2022(): Int {
        return import(
            droidConLondon2022,
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
        return import(
            kotlinConf2023,
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
        return import(
            androidMakers2023,
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
            partners = listOf(
                DPartner(name = "Roquefort", logoUrl = "https://www.fromages.com/media/uploads/fromage/liste_27_1.png", url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
                DPartner(name = "Comté", logoUrl = "https://www.fromages.com/media/uploads/fromage/liste_808_1.png", url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
                DPartner(name = "Camembert", logoUrl = "https://www.fromages.com/media/uploads/fromage/liste_75_1.png", url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
            )
        )
    }

    private suspend fun import(url: String, config: DConfig, venue: DVenue, partners: List<DPartner> = emptyList()): Int {
        val data = getJsonUrl(url)
        val day1 = LocalDate(2023, 4, 27)
        val day2 = LocalDate(2023, 4, 28)
        var time = day1.atTime(9, 30)
        var roomIndex = 0
        val fakeRooms = listOf("moebius", "blin", "202", "204")

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
            var start = time
            if (start > LocalDateTime(2023, 4, 27, 18, 0)) {
                start = day2.atTime(9,30)
            }
            val end  = start.toInstant(TimeZone.UTC).plus(1.hours).toLocalDateTime(TimeZone.UTC)

            DSession(
                id = it.get("id").asString,
                type = if (it.get("isServiceSession").cast()) "service" else "talk",
                title = it.get("title").asString,
                description = it.get("description")?.asString,
                language = "en-US",
                start = start,
                end = end,
                complexity = null,
                feedbackId = null,
                tags = it.get("categoryItems").asList.mapNotNull { categoryId ->
                    categories.get(categoryId)?.asString
                },
                rooms = listOf(fakeRooms.get(roomIndex++)),
                speakers = it.get("speakers").asList.map { it.asString },
                shortDescription = null,
            ).also {
                time = end
                if (roomIndex >= fakeRooms.size) {
                    roomIndex = 0
                }
            }
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
        return DataStore().write(
            sessions = sessions.sortedBy { it.start },
            rooms = rooms,
            speakers = speakers,
            partnerGroups = listOf(DPartnerGroup("1", partners)),
            config = config,
            venues = listOf(venue)
        )
    }
}