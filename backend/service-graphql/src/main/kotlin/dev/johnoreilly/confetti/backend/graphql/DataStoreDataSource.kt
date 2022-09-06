package dev.johnoreilly.confetti.backend.graphql

import dev.johnoreilly.confetti.backend.datastore.DLink
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class DataStoreDataSource(private val conf: String) : DataSource {
    private val datastore = DataStore()

    private val _config: Configuration by lazy {
        val dconfig = datastore.readConfig(conf)

        Configuration(timezone = dconfig.timeZone)
    }

    private val _rooms by lazy {
        datastore.readRooms(conf).map {
            Room(
                id = it.id,
                name = it.name,
                capacity = null
            )
        }
    }

    private val _speakers by lazy {
        datastore.readSpeakers(conf).map { it.toSpeaker() }
    }
    override fun rooms(): List<Room> {
        return _rooms
    }

    override fun venue(id: String): Venue {
        TODO()
    }

    override fun configuration(): Configuration {
        return _config
    }
    override fun sessions(first: Int, after: String?): SessionConnection {
        val page = datastore.readSessions(
            conf = conf,
            limit = first,
            cursor = after
        )

        return SessionConnection(
            nodes = page.items.map {it.toSession() },
            pageInfo = PageInfo(endCursor = page.nextPageCursor)
        )
    }

    private fun DSession.toSession(): Session {
        return Session(
            id = id,
            title = title,
            description = description,
            language = language,
            speakerIds = speakers.toSet(),
            tags = tags,
            startInstant = start.toInstant(TimeZone.of(_config.timezone)),
            endInstant = end.toInstant(TimeZone.of(_config.timezone)),
            roomIds = rooms.toSet()
        )
    }

    override fun speakers(): List<Speaker> {
        return _speakers
    }

    private fun DSpeaker.toSpeaker(): Speaker {
        return Speaker(
            id = id,
            name = name,
            bio = bio,
            company = company,
            socials = links.map { it.toSocial() },
            photoUrl = photoUrl
        )
    }

    private fun DLink.toSocial(): Social {
        return Social(
            icon = null,
            link = url,
            name = key
        )
    }
    override fun partnerGroups(): List<PartnerGroup> {
        return emptyList()
    }
}
