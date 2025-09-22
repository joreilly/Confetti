import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DRoom
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DVenue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import dev.johnoreilly.confetti.backend.import.getJsonUrl
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
import net.mbonnin.bare.graphql.asList
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.asString
import net.mbonnin.bare.graphql.cast

object DPE {
    fun import2025(): Int = runBlocking {
        val url = "https://storage.googleapis.com/martin-public/dpe_2025.json"

        val data = getJsonUrl(url)

        val visitedSpeakers = mutableMapOf<String, DSpeaker>()
        val sessions = data.asList.mapIndexed { index, it ->
            val s = it.asMap
            val avatars = s.get("speakerAvatars").asList
            val speakers = s.get("speakers").asList

            speakers.forEachIndexed { index, it ->
                val name = it.asString
                visitedSpeakers.merge(
                    name, DSpeaker(
                        id = name,
                        name = name,
                        bio = null,
                        tagline = null,
                        company = null,
                        companyLogoUrl = null,
                        city = null,
                        links = emptyList(),
                        photoUrl = avatars.getOrNull(index)?.toString(),
                        sessions = listOf(index.toString())
                    )
                ) { old, new ->
                    old.copy(
                        sessions = (old.sessions.orEmpty() + new.sessions.orEmpty()).distinct()
                    )
                }
            }
            DSession(
                id = index.toString(),
                type = "talk",
                title = s.get("title").cast(),
                description = s.get("description").cast(),
                shortDescription = null,
                language = null,
                start = localDateTime(s.get("day").cast(), s.get("start").cast()),
                end = localDateTime(s.get("day").cast(), s.get("end").cast()),
                complexity = null,
                feedbackId = null,
                tags = emptyList(),
                rooms = listOf(s.get("room").cast()),
                speakers = speakers.cast(),
                links = emptyList(),
            )
        }

        DataStore().write(
            sessions = sessions,
            rooms = sessions.flatMap { it.rooms }.distinct().map { DRoom(it, it) },
            speakers = visitedSpeakers.values.toList(),
            partnerGroups = emptyList(),
            config = DConfig(
                id = ConferenceId.DPE2025.id,
                name = "DPE summit",
                timeZone = "America/Los_Angeles",
                days = listOf(LocalDate(2025, 9, 23), LocalDate(2025, 9,24)),
                themeColor = "0xff209BC4"
            ),
            venues = listOf(DVenue(
                id = "midway",
                name = "Midway San Francisco",
                address = "900 Marin St, San Francisco, CA 94124, United States",
                latitude = 37.7492816,
                longitude = -122.3882693,
                description = mapOf("en" to "Nestled in San Francisco’s Dogpatch district, The Midway is a sprawling 40,000-square-foot hub of creativity and innovation, welcoming all to discover, create, interact, and be moved. Our vibrant venue celebrates the confluence of music, art, cutting-edge technology, and culinary arts, offering an array of performances, workshops, and exhibits that spark inspiration at every corner. At The Midway, we’re dedicated to curating an enriching, thought-provoking journey for every visitor, where the unexpected becomes the norm."),
                imageUrl = "https://corporate.themidwaysf.com/wp-content/uploads/1697644799134-1080x810.jpeg",
                floorPlanUrl = null
            ))
        )
    }
}

private fun localDateTime(day: String, time: String): LocalDateTime {
    val timeFormat = LocalTime.Format {
        hour()
        char(':')
        minute()
    }

    val dateFormat = LocalDate.Formats.ISO // yyyy-MM-dd

    val date = dateFormat.parse(day)
    val time = timeFormat.parse(time)

    return LocalDateTime(date, time)
}