package dev.johnoreilly.confetti.backend.graphql

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

class TestDataSource : DataSource {
    private val rooms = listOf(
        Room(
            id = "room0",
            name = "Room 0",
            capacity = null
        )
    )

    override fun rooms(): List<Room> {
        return rooms
    }

    private val nowInstant = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    private val speakers = listOf(
        Speaker(
            id = "speaker0",
            name = "speaker 0",
            bio = "Bio updated at $nowInstant",
            tagline = null,
            company = null,
            companyLogoUrl = null,
            city = null,
            socials = emptyList(),
            photoUrl = null,
            sessionIds = emptyList()
        )
    )

    private val start = LocalDateTime(2032, 1, 1, 12, 0, 0)

    override fun sessions(
        first: Int,
        after: String?,
        filter: SessionFilter?,
        orderBy: SessionOrderBy?
    ): SessionConnection {
        val from: Int = after?.toIntOrNull() ?: 0
        val to = from + first

        return SessionConnection(
            nodes = from.until(to).map {
                val startInstant = start.toInstant(TimeZone.UTC) + it.hours
                val endInstant = startInstant + 1.hours

                Session(
                    id = "session$it",
                    title = "#$it - ${nowInstant}",
                    shortDescription = "shortDescription updated at $nowInstant",
                    speakerIds = speakers.map { it.id }.toSet(),
                    description = "Description  updated at $nowInstant",
                    language = null,
                    tags = emptyList(),
                    startInstant = startInstant,
                    endInstant = endInstant,
                    startsAt = startInstant.toLocalDateTime(TimeZone.UTC),
                    endsAt = startInstant.toLocalDateTime(TimeZone.UTC),
                    roomIds = rooms.map { it.id }.toSet(),
                    complexity = null,
                    feedbackId = null,
                    type = "talk"
                )
            },
            pageInfo = PageInfo(
                endCursor = to.toString()
            )
        )
    }

    override fun sessions(ids: List<String>): List<Session> {
        return emptyList()
    }

    override fun speakers(): List<Speaker> {
        return speakers
    }

    override fun venues(): List<Venue> {
        return listOf(
            Venue(
                id = "venue0",
                name = "Venue 0",
                latitude = null,
                longitude = null,
                address = null,
                imageUrl = null,
                floorPlanUrl = null,
                descriptions = emptyMap()
            )
        )
    }

    override fun partnerGroups(): List<PartnerGroup> {
        return emptyList()
    }

    override fun conference(): Conference {
        return Conference(
            id = "test",
            name = "Test Conference",
            timezone = TimeZone.UTC.id,
            days = listOf(
                start.date,
                (start.toInstant(TimeZone.UTC) + 24.hours).toLocalDateTime(TimeZone.UTC).date
            )
        )
    }

    override fun bookmarks(): Set<String> {
        return emptySet()
    }

    override fun addBookmark(sessionId: String): Set<String> {
        return emptySet()
    }

    override fun removeBookmark(sessionId: String): Set<String> {
        return emptySet()
    }

    override fun speaker(id: String): Speaker {
        return speakers[0]
    }
}