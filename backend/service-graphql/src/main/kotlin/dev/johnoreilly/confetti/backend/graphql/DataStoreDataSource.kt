package dev.johnoreilly.confetti.backend.graphql

import confetti.type.LinkType
import confetti.type.OrderByDirection
import confetti.type.SessionField
import confetti.type.SessionFilterInput
import confetti.type.SessionOrderByInput
import dev.johnoreilly.confetti.backend.datastore.DComparatorGe
import dev.johnoreilly.confetti.backend.datastore.DComparatorLe
import dev.johnoreilly.confetti.backend.datastore.DConfig
import dev.johnoreilly.confetti.backend.datastore.DDirection
import dev.johnoreilly.confetti.backend.datastore.DFilter
import dev.johnoreilly.confetti.backend.datastore.DLink
import dev.johnoreilly.confetti.backend.datastore.DOrderBy
import dev.johnoreilly.confetti.backend.datastore.DPartner
import dev.johnoreilly.confetti.backend.datastore.DPartnerGroup
import dev.johnoreilly.confetti.backend.datastore.DSession
import dev.johnoreilly.confetti.backend.datastore.DSpeaker
import dev.johnoreilly.confetti.backend.datastore.DataStore
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

internal class DataStoreDataSource(private val conf: String, private val uid: String? = null) : DataSource {
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
        datastore.readSpeakers(conf).map { it.toSpeaker() }.sortedBy { it.name }
    }

    private val sessionCache = mutableMapOf<String, Session>()

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

    override fun addBookmark(sessionId: String): Set<String> {
        if (uid == null) {
            return emptySet()
        }

        return datastore.addBookmark(uid, conf, sessionId)
    }

    override fun removeBookmark(sessionId: String): Set<String> {
        if (uid == null) {
            return emptySet()
        }

        return datastore.removeBookmark(uid, conf, sessionId)
    }

    override fun speaker(id: String): Speaker {
        return datastore.readSpeaker(conf, id).toSpeaker()
    }

    override fun sessions(
        first: Int,
        after: String?,
        filter: SessionFilterInput?,
        orderBy: SessionOrderByInput?
    ): SessionConnection {
        val page = datastore.readSessions(
            conf = conf,
            limit = first,
            cursor = after,
            filters = buildList {
                filter?.startsAt?.getOrNull()?.before?.let {
                    add(DFilter("start", DComparatorLe, it.toString()))
                }
                filter?.startsAt?.getOrNull()?.after?.let {
                    add(DFilter("start", DComparatorGe, it.toString()))
                }
                filter?.endsAt?.getOrNull()?.before?.let {
                    add(DFilter("end", DComparatorLe, it.toString()))
                }
                filter?.endsAt?.getOrNull()?.after?.let {
                    add(DFilter("end", DComparatorLe, it.toString()))
                }
            },
            orderBy = orderBy?.let {
                DOrderBy(
                    field = it.field.actualFieldName(),
                    direction = it.direction.toDDirection()
                )
            }
        )

        val sessions = page.items.map { it.toSession() }

        sessionCache.putAll(sessions.associateBy { it.id })
        return SessionConnection(
            nodes = sessions,
            pageInfo = PageInfo(endCursor = page.nextPageCursor)
        )
    }

    private fun SessionField.actualFieldName(): String = when(this) {
        SessionField.STARTS_AT -> "start"
    }

    override fun sessions(
        ids: List<String>,
    ): List<Session> {
        if (sessionCache.keys.containsAll(ids)) {
            return ids.mapNotNull { sessionCache.get(it) }
        }
        val sessions = datastore.readSessions(
            conf = conf,
            ids = ids,
        ).map { it.toSession() }
        sessionCache.putAll(sessions.associateBy { it.id })
        return sessions
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
            type = type,
            links = links.map {
                Link(
                    type = try {
                        LinkType.valueOf(it.key)
                    } catch (e: Exception) {
                        LinkType.Other
                    }, url = it.url
                )
            }
        )
    }

    override fun speakers(first: Int, after: String?): SpeakerConnection {
        val drop = if (after == null) {
            0
        } else {
            _speakers.indexOfFirst { it.id == after }
                .also {
                    if (it == -1) {
                        return SpeakerConnection(emptyList(), PageInfo(null))
                    }
                } + 1
        }
        return SpeakerConnection(
            nodes = _speakers.drop(drop).take(first),
            pageInfo = PageInfo(endCursor = _speakers.getOrNull(drop + first)?.id)
        )
    }

    override fun speakers(
        ids: List<String>,
    ): List<Speaker> {
        return _speakers.filter { it.id in ids }
    }


    override fun venues(): List<Venue> {
        return _venues
    }

    private fun DSpeaker.toSpeaker(): Speaker {
        return Speaker(
            id = id,
            name = name,
            bio = bio,
            tagline = tagline,
            company = company,
            socials = links.map { it.toSocial() },
            photoUrl = photoUrl,
            companyLogoUrl = companyLogoUrl,
            city = city,
            sessionIds = sessions.orEmpty(),
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
internal fun OrderByDirection.toDDirection() = when(this) {

    OrderByDirection.ASCENDING -> DDirection.ASCENDING
    OrderByDirection.DESCENDING -> DDirection.DESCENDING
}
