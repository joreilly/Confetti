package dev.johnoreilly.confetti.backend.graphql

import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class DataStoreDataSource(private val conf: String) : DataSource {
    private val datastore = DataStore()

    private val _config: Configuration by lazy {
        val dconfig = datastore.readConfig(conf)

        Configuration(
            name = dconfig.name,
            timezone = dconfig.timeZone
        )
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


    private val _venues: List<Venue> by lazy {
        datastore.readVenues(conf).map {
            Venue(
                id = it.id,
                name = it.name,
                latitude = it.latitude,
                longitude = it.longitude,
                address = it.address,
                imageUrl = it.imageUrl,
                descriptions = it.description
            )
        }
    }

    private val _speakers by lazy {
        datastore.readSpeakers(conf).map { it.toSpeaker() }
    }
    override fun rooms(): List<Room> {
        return _rooms
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
            roomIds = rooms.toSet(),
            complexity = complexity,
            feedbackId = feedbackId,
            type = type
        )
    }

    override fun speakers(): List<Speaker> {
        return _speakers
    }

    override fun venues(): List<Venue> {
        return _venues
    }

    private fun DSpeaker.toSpeaker(): Speaker {
        return Speaker(
            id = id,
            name = name,
            bio = bio,
            company = company,
            socials = links.map { it.toSocial() },
            photoUrl = photoUrl,
            companyLogoUrl = companyLogoUrl,
            city = city
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
        return datastore.readPartnerGroups(conf).map {
            it.toPartnerGroup()
        }
    }

    private fun DPartnerGroup.toPartnerGroup(): PartnerGroup {
        return PartnerGroup(
            title = key,
            partners = partners.map { it.toPartner() }
        )
    }

    private fun DPartner.toPartner(): Partner {
        return Partner(
            name = name,
            logoUrl = logoUrl,
            url = url
        )
    }
}
