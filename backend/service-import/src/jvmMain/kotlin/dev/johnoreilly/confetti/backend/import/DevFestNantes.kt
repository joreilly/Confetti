@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.backend.import

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlTaggedNode
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DLink
import dev.johnoreilly.confetti.backend.datastore.DPartner
import dev.johnoreilly.confetti.backend.datastore.DPartnerGroup
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DVenue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.asList
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.asString
import net.mbonnin.bare.graphql.toAny
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val timeZone = "Europe/Paris"

private val okHttpClient = OkHttpClient.Builder()
    .build()

private val baseUrl2022 = "https://raw.githubusercontent.com/GDG-Nantes/Devfest2022/master/"
private val baseUrl2023 = "https://raw.githubusercontent.com/GDG-Nantes/Devfest2023/main/"
private val baseUrl2024 = "https://raw.githubusercontent.com/GDG-Nantes/Devfest2024/main/"
private val baseUrl2025 = "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/"
private val json = Json {
    ignoreUnknownKeys = true
}

suspend fun importDefvestNantes2022() =
    DevFestNantes(
        baseUrl2022,
        "Devfest2022",
        "master",
        ConferenceId.DevFestNantes2022.id,
        listOf(
            LocalDate(2022, 10, 20),
            LocalDate(2022, 10, 21)
        ),
        "https://raw.githubusercontent.com/GDG-Nantes/Devfest2022/master/src/images/home/album/wide/amphi.jpg",
        "https://raw.githubusercontent.com/GDG-Nantes/Devfest2022/master/src/images/plan-cite-blanc.png",
        ""
    ).import()

suspend fun importDefvestNantes2023() = DevFestNantes(
    baseUrl2023,
    "Devfest2023",
    "main",
    ConferenceId.DevFestNantes2023.id,
    listOf(
        LocalDate(2023, 10, 19),
        LocalDate(2023, 10, 20)
    ),
    "https://raw.githubusercontent.com/GDG-Nantes/Devfest2023/main/src/images/home/album/wide/800.jpg",
    "https://raw.githubusercontent.com/GDG-Nantes/Devfest2023/main/src/images/plan-cite-transparent.png",
    ""
).import()

suspend fun importDefvestNantes2024() = DevFestNantes(
    baseUrl2024,
    "Devfest2024",
    "main",
    ConferenceId.DevFestNantes2024.id,
    listOf(
        LocalDate(2024, 10, 17),
        LocalDate(2024, 10, 18)
    ),
    "https://raw.githubusercontent.com/GDG-Nantes/Devfest2024/main/src/images/home/album/wide/2000.jpg",
    "https://raw.githubusercontent.com/GDG-Nantes/Devfest2024/main/src/images/plan-cite-transparent.png",
    "https://raw.githubusercontent.com/GDG-Nantes/Devfest2024/main/static"
).import()

suspend fun importDefvestNantes2025() = DevFestNantes(
    baseUrl2025,
    "Devfest2025",
    "main",
    ConferenceId.DevFestNantes2025.id,
    listOf(
        LocalDate(2025, 10, 16),
        LocalDate(2025, 10, 17)
    ),
    "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/home/album/equipe-amphi.jpg",
    "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/plan-cite-transparent.png",
    "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/"
).import()


class DevFestNantes(
    private val baseUrl: String,
    private val confId: String,
    private val mainBranch: String,
    private val id: String,
    private val days: List<LocalDate>,
    private val venueImageUrl: String,
    private val venueFloorPlanUrl: String,
    private val speakerImagesBaseUrl: String,
) {
    private suspend fun getUrl(url: String): String {
        val request = Request(url.toHttpUrl())
        val response = okHttpClient.newCall(request).executeAsync()

        return response.use {
            check(response.isSuccessful) {
                "Cannot get $url: ${response.body.string()}"
            }

            withContext(Dispatchers.IO) {
                response.body.string()
            }
        }
    }

    private suspend fun getGithubFile(name: String): String {
        return getUrl("$baseUrl$name")
    }

    private suspend fun getJsonUrl(url: String) = Json.parseToJsonElement(getUrl(url)).toAny()
    private suspend fun getJsonGithubFile(name: String) =
        Json.parseToJsonElement(getGithubFile(name)).toAny()

    private suspend fun getFiles(ref: String): List<Map<String, Any?>> {
        return getJsonUrl("https://api.github.com/repos/GDG-Nantes/$confId/git/trees/$ref")
            .asMap
            .get("tree")
            .asList
            .map { it.asMap }
    }

    private val sessionIdsWithoutRoom = mutableSetOf<String>()

    private suspend fun listFiles(path: String): List<String> {
        var files: List<Map<String, Any?>> = getFiles(mainBranch)

        path.split("/").forEach { comp ->
            val sha = files.first { it.get("path") == comp }.get("sha").asString
            files = getFiles(sha)
        }

        return files.map { it.get("path").asString }
            .map { "$path/$it" }
    }

    private suspend fun listYamls(path: String): List<String> =
        listFiles(path).filter { it.endsWith(".yml") }

    private fun Map<String, Any?>.startTime(): LocalDateTime {
        val day = if (get("key").asString.startsWith("day-1")) {
            days[0]
        } else {
            days[1]
        }

        return day.atTime(LocalTime.parse(get("start").toString()))
    }

    private val UNKNOWN_END = LocalDateTime(0, 1, 1, 0, 0)

    internal suspend fun import(): Int {
        val slots = getJsonGithubFile("data/slots.json").asMap.get("slots").asList.map { it.asMap }
        val categories =
            getJsonGithubFile("data/categories.json").asMap.get("categories").asList.map { it.asMap }
        val partners = getJsonGithubFile("data/partners.json")

        val roomIds = mutableSetOf<String>()
        var sessions = listYamls("data/sessions").mapNotNull { session ->
            val talk = Yaml.default.parseToYamlNode(getGithubFile(session)).toAny().asMap

            if (talk.get("cancelled") == "true") {
                return@mapNotNull null
            }
            val slot = slots.first { it.get("key") == talk.get("slot") }
            val localDateTime = slot.startTime()
            val roomId = talk.get("room").asString

            roomIds.add(roomId)

            /**
             * construct a valid ID for DataStore
             */
            val id = session.substringAfterLast("/")
                .replace("_", "")
                .substringBeforeLast(".")
                .take(64)
            DSession(
                id = id,
                title = talk.get("title").asString,
                description = talk.get("abstract")?.asString,
                language = when (talk.get("language").asString.lowercase()) {
                    "french" -> "fr-FR"
                    else -> "en-US"
                },
                speakers = talk.get("speakers").asList.map { it.asString },
                tags = talk.get("tags").asList.mapNotNull { tagId ->
                    categories.firstOrNull { it.get("id") == tagId.asString }
                        ?.get("label")?.asString
                },
                start = localDateTime,
                // The YAMLs do not have an end time
                end = talk.get("talkType")?.asString?.toDuration()?.let { localDateTime + it } ?: UNKNOWN_END,
                complexity = talk.get("complexity")?.asString,
                feedbackId = talk.get("openfeedbackId")?.asString,
                rooms = listOf(roomId),
                type = talk.get("talkType")?.asString ?: "talk",
                shortDescription = null,
                links = emptyList()
            )
        } + slotSessions(slots)

        val sortedSessions = sessions.sortedBy { it.start }

        sessions = sessions.map { session ->
            if (session.end == UNKNOWN_END) {
                val found = sortedSessions.firstOrNull { candidate ->
                    candidate.start > session.start
                        && candidate.start.dayOfMonth == session.start.dayOfMonth
                        && candidate.id != "day-1-party"
                        && candidate.rooms.intersect(session.rooms.toSet()).isNotEmpty()
                }

                val end = found?.start ?: (session.start + 20.minutes)
                session.copy(end = end)
            } else {
                session
            }
        }
        sessions = sessions.map {
            /**
             * Remove the room for lunch & break sessions
             * See https://github.com/GDG-Nantes/DevfestNantesMobile/issues/85
             */
            if (sessionIdsWithoutRoom.contains(it.id)) {
                it.copy(rooms = emptyList())
            } else {
                it
            }
        }
        val allImages = listFiles("src/images/partners").map { it.substringAfterLast("/") }
        val partnerGroups = partners.asMap.entries.map {
            DPartnerGroup(
                key = it.key,
                partners = it.value.asList.map { it.asMap }.map {
                    val title = it.get("title").asString
                    val id = it.get("id").asString
                    val image = allImages.firstOrNull { it.substringBeforeLast(".") == id }
                    DPartner(
                        name = title,
                        logoUrl = (image?.let { "$baseUrl/src/images/partners/$it" })
                            ?: error("No partner image for $id"),
                        url = it.get("website")?.asString ?: ""
                    )
                }
            )
        }
        val speakers = listYamls("data/speakers").map {
            val speaker = Yaml.default.parseToYamlNode(getGithubFile(it)).toAny().asMap

            DSpeaker(
                id = speaker.get("key").asString,
                name = speaker.get("name").asString,
                bio = speaker.get("bio")?.asString,
                tagline = null,
                company = speaker.get("company")?.asString,
                links = speaker.get("socials")?.asMap?.entries.orEmpty().map {
                    val handle = it.value.asString.trim()
                    val url = when (it.key) {
                        "github" -> "https://github.com/$handle"
                        "linkedin" -> "https://www.linkedin.com/$handle"
                        "twitter" -> "https://twitter.com/" + handle.trimStart('@')
                        "facebook" -> "https://www.facebook.com/$handle"
                        else -> ""
                    }
                    DLink(key = it.key, url = url)
                },
                photoUrl = speaker.get("photoUrl")?.asString?.let { "${speakerImagesBaseUrl}$it" },
                companyLogoUrl = speaker.get("companyLogo")?.asString?.let { "${baseUrl}src$it" },
                city = speaker.get("city")?.asString,
            )
        }

        val rooms = roomIds.map {
            DRoom(
                id = it,
                name = it,
            )
        }

        val config = DConfig(
            id = id,
            name = "DevFest Nantes",
            timeZone = timeZone,
            days = days,
            themeColor = "0xffFB5C49"
        )

        DataStore().write(
            sessions = sessions.sortedBy { it.start },
            rooms = rooms,
            speakers = speakers,
            partnerGroups = partnerGroups,
            config = config,
            venues = listOf(
                DVenue(
                    id = "main",
                    name = "Cité des Congrès de Nantes",
                    address = "5 rue de Valmy, 44000 Nantes",
                    description = mapOf(
                        "en" to "Located in the center of Nantes, the event takes place in the \"Cité des Congrès\" with more than 3000m² of conference rooms, hand's on and networking space…",
                        "fr" to "Située en plein cœur de ville, La Cité des Congrès de Nantes propose pour le DevFest Nantes plus de 3000m² de salles de conférences, codelabs et lieu de rencontre…",
                    ),
                    latitude = 47.21308725112951,
                    longitude = -1.542622837466317,
                    imageUrl = venueImageUrl,
                    floorPlanUrl = venueFloorPlanUrl
                )
            )
        )
        return sessions.size
    }

    private fun slotSessions(slots: List<Map<String, Any?>>): List<DSession> {
        /**
         * Some of the logic that determines Rooms is in code so I just hardcode the rules here
         * See https://github.com/GDG-Nantes/Devfest2022/blob/d82c0b815d6e67b61db7cf9faf61c2069d875abc/src/components/schedule/large.tsx#L60
         */
        return slots.mapNotNull {
            val id = it.get("key").asString
            val type = it.get("type").asString
            when {
                id == "day-1-party" -> {
                    sessionIdsWithoutRoom.add(id)
                    DSession(
                        id = id,
                        title = "Party",
                        description = "Party",
                        language = "fr-FR",
                        speakers = emptyList(),
                        tags = emptyList(),
                        start = it.startTime(),
                        end = UNKNOWN_END,
                        rooms = ALL_ROOMS,
                        type = "party",
                        complexity = null,
                        feedbackId = null,
                        shortDescription = null,
                        links = emptyList()
                    )
                }

                type == "opening" -> {
                    sessionIdsWithoutRoom.add(id)
                    DSession(
                        id = id,
                        title = "Opening",
                        description = "Opening",
                        language = "fr-FR",
                        speakers = emptyList(),
                        tags = emptyList(),
                        start = it.startTime(),
                        end = UNKNOWN_END,
                        rooms = ALL_ROOMS,
                        type = "opening",
                        complexity = null,
                        feedbackId = null,
                        shortDescription = null,
                        links = emptyList()
                    )
                }

                type == "keynote" -> {
                    DSession(
                        id = id,
                        title = "Keynote",
                        description = "Keynote",
                        language = "fr-FR",
                        speakers = emptyList(),
                        tags = emptyList(),
                        start = it.startTime(),
                        end = UNKNOWN_END,
                        rooms = listOf(ROOM_JULES_VERNE),
                        type = "keynote",
                        complexity = null,
                        feedbackId = null,
                        shortDescription = null,
                        links = emptyList()
                    )
                }

                type == "break" -> {
                    sessionIdsWithoutRoom.add(id)
                    val notForCodelab =
                        it.get("display")?.asMap?.containsKey("notForCodelab") ?: false
                    DSession(
                        id = id,
                        title = "Break",
                        description = "Break",
                        language = "fr-FR",
                        speakers = emptyList(),
                        tags = emptyList(),
                        start = it.startTime(),
                        end = UNKNOWN_END,
                        rooms = if (notForCodelab) FIRST_4_ROOMS else ALL_ROOMS,
                        type = "break",
                        complexity = null,
                        feedbackId = null,
                        shortDescription = null,
                        links = emptyList()
                    )
                }

                type == "lunch" -> {
                    sessionIdsWithoutRoom.add(id)
                    DSession(
                        id = id,
                        title = "Lunch",
                        description = "Lunch",
                        language = "fr-FR",
                        speakers = emptyList(),
                        tags = emptyList(),
                        start = it.startTime(),
                        end = UNKNOWN_END,
                        rooms = ALL_ROOMS,
                        type = "lunch",
                        complexity = null,
                        feedbackId = null,
                        shortDescription = null,
                        links = emptyList()
                    )
                }

                else -> null
            }
        }

    }

    private val ROOM_JULES_VERNE = "Jules Verne"
    private val ROOM_TITAN = "Titan"
    private val ROOM_BELEM = "Belem"
    private val ROOM_TOUR_DE_BRETAGNE = "Tour de Bretagne"
    private val ROOM_LES_MACHINES = "Les Machines"
    private val ROOM_HANGAR = "Hangar"
    private val ROOM_L_ATELIER = "L'Atelier"
    private val FIRST_4_ROOMS = listOf(
        ROOM_JULES_VERNE,
        ROOM_TITAN,
        ROOM_BELEM,
        ROOM_TOUR_DE_BRETAGNE
    )
    private val FIRST_6_ROOMS = FIRST_4_ROOMS + listOf(
        ROOM_LES_MACHINES,
        ROOM_HANGAR,
    )
    private val ALL_ROOMS = FIRST_6_ROOMS + listOf(
        ROOM_L_ATELIER
    )

    private operator fun LocalDateTime.plus(duration: Duration): LocalDateTime {
        return toInstant(TimeZone.UTC).plus(duration).toLocalDateTime(TimeZone.UTC)
    }

    private fun YamlNode.toAny(): Any? {
        return when (this) {
            is YamlNull -> null
            is YamlScalar -> this.content
            is YamlList -> this.items.map {
                it.toAny()
            }

            is YamlMap -> this.entries.map {
                it.key.content to it.value.toAny()
            }.toMap()

            is YamlTaggedNode -> TODO()
        }
    }
}

private fun String.toDuration(): Duration? {
    return when(this) {
        "quickie" -> 20.minutes
        "conference" -> 50.minutes
        "codelab" -> 120.minutes
        else -> {
            println("Unknown type: $this")
            null
        }
    }
}
