package dev.johnoreilly.confetti.backend.graphql

import dev.johnoreilly.confetti.backend.datastore.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

fun DConfig.toConference(): Conference {
    return Conference(
        id = id,
        name = name,
        timezone = timeZone,
        days = days,
    )
}
class DataStoreDataSource(private val conf: String, private val uid: String? = null) : DataSource {
    private val datastore = DataStore()

    private val _config: Conference by lazy {
        datastore.readConfig(conf).toConference()
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
                descriptions = it.description,
                floorPlanUrl = it.floorPlanUrl,
            )
        }
    }

    private val _speakers by lazy {
        datastore.readSpeakers(conf).map { it.toSpeaker() }
    }
    override fun rooms(): List<Room> {
        return _rooms
    }

    override fun conference(): Conference {
        return _config
    }

    override fun bookmarks(): Set<String> {
        if (uid == null) {
            return emptySet()
        }
        return datastore.readBookmarks(uid, conf)
    }

    override fun addBookmark(sessionId: String) {
        if (uid == null) {
            return
        }

        datastore.addBookmark(uid, conf, sessionId)
    }

    override fun removeBookmark(sessionId: String): Boolean {
        if (uid == null) {
            return false
        }

        return datastore.removeBookmark(uid, conf, sessionId)
    }

    override fun sessions(
        first: Int,
        after: String?,
        filter: SessionFilter?,
        orderBy: SessionOrderBy?
    ): SessionConnection {
        val page = datastore.readSessions(
            conf = conf,
            limit = first,
            cursor = after,
            filters = buildList {
                filter?.startsAt?.before?.let {
                    add(DFilter("start", DComparatorLe, it.toString()))
                }
                filter?.startsAt?.after?.let {
                    add(DFilter("start", DComparatorGe, it.toString()))
                }
                filter?.endsAt?.before?.let {
                    add(DFilter("end", DComparatorLe, it.toString()))
                }
                filter?.endsAt?.after?.let {
                    add(DFilter("end", DComparatorLe, it.toString()))
                }
            },
            orderBy = orderBy?.let { DOrderBy(field = it.field.value, direction = it.direction.toDDirection()) }
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
            shortDescription = shortDescription ?: description,
            language = language,
            speakerIds = speakers.toSet(),
            tags = tags,
            startInstant = start.toInstant(TimeZone.of(_config.timezone)),
            endInstant = end.toInstant(TimeZone.of(_config.timezone)),
            startsAt = start,
            endsAt = end,
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
