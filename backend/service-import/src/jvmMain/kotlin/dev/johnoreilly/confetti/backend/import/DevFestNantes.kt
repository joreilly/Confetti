package dev.johnoreilly.confetti.backend.import

import com.charleskorn.kaml.*
import dev.johnoreilly.confetti.backend.datastore.*
import dev.johnoreilly.confetti.backend.import.DevFestNantes.plus
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.asList
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.asString
import net.mbonnin.bare.graphql.toAny
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


private val timeZone = "Europe/Paris"
private val okHttpClient = OkHttpClient()
private val baseUrl = "https://raw.githubusercontent.com/GDG-Nantes/Devfest2022/master/"
private val json = Json {
    ignoreUnknownKeys = true
}

object DevFestNantes {
    private fun getUrl(url: String): String {
        return Request.Builder()
            .url(url)
            .build()
            .let {
                okHttpClient.newCall(it).execute().also {
                    check(it.isSuccessful) {
                        "Cannot get $url: ${it.body?.string()}"
                    }
                }
            }.body!!.string()
    }

    private fun getGithubFile(name: String): String {
        return getUrl("$baseUrl$name")
    }

    private fun getJsonUrl(url: String) = Json.parseToJsonElement(getUrl(url)).toAny()
    private fun getJsonGithubFile(name: String) = Json.parseToJsonElement(getGithubFile(name)).toAny()

    private fun getFiles(ref: String): List<Map<String, Any?>> {
        return getJsonUrl("https://api.github.com/repos/GDG-Nantes/Devfest2022/git/trees/$ref")
            .asMap
            .get("tree")
            .asList
            .map { it.asMap }
    }

    private fun listYamls(path: String): List<String> {
        var files: List<Map<String, Any?>> = getFiles("master")

        path.split("/").forEach { comp ->
            val sha = files.first { it.get("path") == comp }.get("sha").asString
            files = getFiles(sha)
        }

        return files.map { it.get("path").asString }.filter { it.endsWith(".yml") }
            .map { "$path/$it" }
    }

    private fun Map<String, Any?>.startTime(): LocalDateTime {
        val day = if (get("key").asString.startsWith("day-1")) {
            "2022-10-20"
        } else {
            "2022-10-21"
        }

        return LocalDateTime.parse("${day}T${get("start")}")
    }

    private val UNKNOWN_END = LocalDateTime(0, 1, 1, 0, 0)

    internal fun import() {
        val slots = getJsonGithubFile("data/slots.json").asMap.get("slots").asList.map { it.asMap }
        val categories =
            getJsonGithubFile("data/categories.json").asMap.get("categories").asList.map { it.asMap }
        val partners = getJsonGithubFile("data/partners.json")

        val roomIds = mutableSetOf<String>()
        var sessions = listYamls("data/sessions").mapIndexed { index, session ->
            val talk = Yaml.default.parseToYamlNode(getGithubFile(session)).toAny().asMap

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
                description = talk.get("abstract").asString,
                language = when (talk.get("language").asString.lowercase()) {
                    "french" -> "fr-FR"
                    else -> "en-US"
                },
                speakers = talk.get("speakers").asList.map { it.asString },
                tags = talk.get("tags").asList.mapNotNull { tagId ->
                    categories.firstOrNull { it.get("id") == tagId.asString }?.get("label")?.asString
                },
                start = localDateTime,
                // The YAMLs do not have and end time
                end = UNKNOWN_END,
                rooms = listOf(roomId),
                type = "talk"
            )
        } + slotSessions(slots)

        val sortedSessions = sessions.sortedBy { it.start }

        sessions = sessions.map { session ->
            if (session.end == UNKNOWN_END) {
                val end = sortedSessions.firstOrNull { candidate ->
                    candidate.start > session.start
                        && candidate.start.dayOfMonth == session.start.dayOfMonth
                        && candidate.rooms.intersect(session.rooms.toSet()).isNotEmpty()
                }?.start ?: (session.start + 40.minutes)
                session.copy(end = end)
            } else {
                session
            }
        }
        val partnerGroups = partners.asMap.entries.map {
            DPartnerGroup(
                key = it.key,
                partners = it.value.asList.map { it.asMap }.map {
                    val title = it.get("title").asString
                    val id = it.get("id").asString
                    DPartner(
                        name = title,
                        logoUrl = "$baseUrl/src/images/partners/$id.png",
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
                company = speaker.get("company")?.asString,
                links = speaker.get("socials").asMap.entries.map {
                    DLink(key = it.key, url = it.value.asString)
                },
                photoUrl = speaker.get("photoUrl")?.asString
            )
        }

        val rooms = roomIds.map {
            DRoom(
                id = it,
                name = it,
            )
        }

        val config = DConfig(
            timeZone = timeZone
        )

        return DataStore().write(
            conf = "devfestnantes",
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
                    imageUrl = "https://devfest.gdgnantes.com/static/6328df241501c6e31393e568e5c68d7e/efc43/amphi.webp"
                )
            )
        )
    }

    private fun slotSessions(slots: List<Map<String, Any?>>): List<DSession> {
        /**
         * Some of the logic that determines Rooms is in code so I just hardcode the rules here
         * See https://github.com/GDG-Nantes/Devfest2022/blob/d82c0b815d6e67b61db7cf9faf61c2069d875abc/src/components/schedule/large.tsx#L60
         */
        return slots.mapNotNull {
            val id = it.get("key").asString
            val type = it.get("type").asString
            when  {
                id == "day-2-pause-2" -> {
                    DSession(
                        id = id,
                        title = "Break",
                        description = "Break",
                        language = "fr-FR",
                        speakers = emptyList(),
                        tags = emptyList(),
                        start = it.startTime(),
                        end = UNKNOWN_END,
                        rooms = FIRST_6_ROOMS,
                        type = "break"
                    )
                }
                id == "day-1-party" -> {
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
                        type = "party"
                    )
                }
                type == "opening" -> {
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
                        type = "opening"
                    )
                }
                type == "keynote" -> {
                    DSession(
                        id =id,
                        title = "Keynote",
                        description = "Keynote",
                        language = "fr-FR",
                        speakers = emptyList(),
                        tags = emptyList(),
                        start = it.startTime(),
                        end = UNKNOWN_END,
                        rooms = listOf(ROOM_JULES_VERNE),
                        type = "keynote"
                    )
                }
                type == "break" -> {
                    val notForCodelab = it.get("display")?.asMap?.containsKey("notForCodelab") ?: false
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
                        type = "break"
                    )
                }
                type == "lunch" -> {
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
                        type = "lunch"
                    )
                }
                else -> null
            }
        }

    }

    private const val ROOM_JULES_VERNE = "Jules Verne"
    private const val ROOM_TITAN = "Titan"
    private const val ROOM_BELEM = "Belem"
    private const val ROOM_TOUR_DE_BRETAGNE = "Tour de Bretagne"
    private const val ROOM_LES_MACHINES = "Les Machines"
    private const val ROOM_HANGAR = "Hangar"
    private const val ROOM_L_ATELIER = "L'Atelier"
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

    private fun keynote(localDateTime: String): DSession {
        val start = LocalDateTime.parse(localDateTime)
        return DSession(
            id = "keynote",
            title = "Keynote",
            description = "Keynote",
            language = "fr-FR",
            speakers = emptyList(),
            tags = emptyList(),
            start = start,
            end = start.plus(40.minutes),
            rooms = listOf(ROOM_JULES_VERNE),
            type = "keynote"
        )
    }

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