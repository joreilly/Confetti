package dev.johnoreilly.confetti.backend.import

import com.charleskorn.kaml.*
import dev.johnoreilly.confetti.backend.datastore.*
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

    internal fun import() {
        val slots = getJsonGithubFile("data/slots.json")
        val categories =
            getJsonGithubFile("data/categories.json").asMap.get("categories").asList.map { it.asMap }
        val partners = getJsonGithubFile("data/partners.json")

        val roomIds = mutableSetOf<String>()
        var sessions = listYamls("data/sessions").mapIndexed { index, session ->
            val talk = Yaml.default.parseToYamlNode(getGithubFile(session)).toAny().asMap

            val slot = slots.asMap.get("slots").asList.map { it.asMap }.first { it.get("key") == talk.get("slot") }
            val day = if (talk.get("slot").asString.startsWith("day-1")) {
                "2022-10-20"
            } else {
                "2022-10-21"
            }
            val localDateTime = LocalDateTime.parse("${day}T${slot.get("start")}")
            val roomId = talk.get("room").asString

            roomIds.add(roomId)

            DSession(
                id = index.toString(),
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
                end = LocalDateTime(0, 1, 1, 0, 0),
                rooms = listOf(roomId),
                type = "talk"
            )
        } + keynote("2022-10-20T09:00") + keynote("2022-10-21T17:20")

        val sortedSessions = sessions.sortedBy { it.start }

        sessions = sessions.map { session ->
            if (session.end.year == 0) {
                val end = sortedSessions.firstOrNull { candidate ->
                    candidate.start > session.start && candidate.start.dayOfMonth == session.start.dayOfMonth
                }?.start ?: (session.start + 40.minutes)
                session.copy(end = end)
            } else {
                session
            }
        }
//        val partnerGroups = partners.asMap.entries.map {
//            PartnerGroup(
//                title = it.key,
//                partners = it.value.asList.map { it.asMap }.map {
//                    Partner(
//                        name = it.get("title").asString,
//                        logoUrl = "https://raw.githubusercontent.com/GDG-Nantes/Devfest2022/master/src/images/partners/${
//                            it.get(
//                                "title"
//                            )
//                        }.png",
//                        url = it.get("website")?.asString ?: ""
//                    )
//                }
//            )
//        }
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
            sessions = sessions,
            rooms = rooms,
            speakers = speakers,
            config = config
        )
    }

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
            rooms = listOf("Jules Verne"),
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