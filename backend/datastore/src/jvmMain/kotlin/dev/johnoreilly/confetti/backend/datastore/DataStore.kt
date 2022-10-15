package dev.johnoreilly.confetti.backend.datastore

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.*
import com.google.datastore.v1.QueryResultBatch
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.*
import java.io.File

class DataStore {
    private val datastore: Datastore
    private val keyFactory: KeyFactory
        get() {
            return datastore.newKeyFactory()
        }

    init {
        val serviceAccountKeyFile = File("/Users/mbonnin/git/Confetti/backend/service_account_key.json")
        datastore = if (serviceAccountKeyFile.exists()) {
            val credentials = serviceAccountKeyFile.inputStream().use {
                GoogleCredentials.fromStream(it)
            }
            DatastoreOptions.newBuilder().setCredentials(credentials).build().service
        } else {
            DatastoreOptions.getDefaultInstance().service
        }
    }

    fun write(
        conf: String,
        sessions: List<DSession>,
        rooms: List<DRoom>,
        speakers: List<DSpeaker>,
        partnerGroups: List<DPartnerGroup>,
        venues: List<DVenue>,
        config: DConfig
    ) {
        datastore.runInTransaction {
            it.put(partnerGroups.toEntity(conf))
            it.put(config.toEntity(conf))
            it.deleteAll(conf, KIND_VENUE)
            it.put(*venues.map { it.toEntity(conf) }.toTypedArray())
            it.deleteAll(conf, KIND_SESSION)
            it.put(*sessions.map { it.toEntity(conf) }.toTypedArray())
            it.deleteAll(conf, KIND_ROOM)
            it.put(*rooms.map { it.toEntity(conf) }.toTypedArray())
            it.deleteAll(conf, KIND_SPEAKER)
            it.put(*speakers.map { it.toEntity(conf) }.toTypedArray())
        }
    }

    private fun DatastoreReaderWriter.deleteAll(conf: String, kind: String) {
        val query: EntityQuery? = Query.newEntityQueryBuilder()
            .setKind(kind)
            .setLimit(100)
            .setFilter(
                StructuredQuery.PropertyFilter.hasAncestor(
                    keyFactory.setKind(KIND_CONF).newKey(conf)
                )
            )
            .build()
        val result = datastore.run(query)

        val keys = mutableListOf<Key>()
        result.forEach {
            keys.add(it.key)
        }

        datastore.delete(*keys.toTypedArray())
    }

    private fun log(message: String) {
        if (false) {
            println(message)
        }
    }

    fun readSessions(conf: String, limit: Int, cursor: String?): DPage<DSession> {
        log("readSessions limit=$limit")
        val query: EntityQuery? = Query.newEntityQueryBuilder()
            .setKind(KIND_SESSION)
            .setLimit(limit)
            .apply {
                if (cursor != null) {
                    setStartCursor(Cursor.fromUrlSafe(cursor))
                }
            }
            .setFilter(
                StructuredQuery.PropertyFilter.hasAncestor(
                    keyFactory.setKind(KIND_CONF).newKey(conf)
                )
            )
            .build()
        val result = datastore.run(query)

        val items = mutableListOf<DSession>()
        result.forEach {
            items.add(it.toSession())
        }

        log("moreResults=${result.moreResults} cursor=${result.cursorAfter.toUrlSafe()}")
        val nextPageCursor = when(result.moreResults) {
            QueryResultBatch.MoreResultsType.NO_MORE_RESULTS -> null
            else -> result.cursorAfter.toUrlSafe()
        }
        return DPage(items, nextPageCursor = nextPageCursor)
    }

    fun readPartnerGroups(conf: String): List<DPartnerGroup> {
        log("readPartnerGroups")
        return datastore.get(
            keyFactory
                .setKind(KIND_PARTNERGROUPS)
                .addAncestor(PathElement.of(KIND_CONF, conf))
                .newKey(THE_PARTNERGROUPS)
        ).getString(THE_PARTNERGROUPS).let {
            Json.parseToJsonElement(it).toAny().asList.map {
                it.asMap.toPartnerGroup()
            }
        }
    }

    fun readConfig(conf: String): DConfig {
        log("readConfig")
        return datastore.get(
            keyFactory
                .setKind(KIND_CONFIG)
                .addAncestor(PathElement.of(KIND_CONF, conf))
                .newKey(THE_CONFIG)
        ).toConfig()
    }

    fun readSession(conf: String, id: String): DSession {
        log("readSession")
        return datastore.get(
            keyFactory
                .setKind(KIND_SESSION)
                .addAncestor(PathElement.of(KIND_CONF, conf))
                .newKey(id)
        )
            .toSession()
    }

    private fun Entity.toRoom(): DRoom {
        return DRoom(
            id = key.name,
            name = getString("name")
        )
    }

    private fun Entity.toVenue(): DVenue {
        return DVenue(
            id = key.name,
            name = getString("name"),
            address = getStringOrNull("address"),
            latitude = getDoubleOrNull("latitude"),
            longitude = getDoubleOrNull("longitude"),
            description = (Json.parseToJsonElement(getString("description")).toAny() as Map<String, String>),
            imageUrl = getStringOrNull("imageUrl")
        )
    }

    private fun Entity.toSpeaker(): DSpeaker {
        return DSpeaker(
            id = key.name,
            name = getString("name"),
            bio = getStringOrNull("bio"),
            company = getStringOrNull("company"),
            links = getList<StringValue>("links").map {
                Json.parseToJsonElement(it.get()).toAny().asMap.toLink()
            },
            photoUrl = getStringOrNull("photoUrl"),
            companyLogoUrl = getStringOrNull("companyLogoUrl"),
            city = getStringOrNull("city"),
        )
    }

    private fun Map<String, Any?>.toLink(): DLink {
        return DLink(
            key = get("key").asString,
            url = get("url").asString
        )
    }

    private fun Entity.toSession(): DSession {
        return DSession(
            id = key.name,
            type = getString("type"),
            title = getString("title"),
            description = getStringOrNull("description"),
            language = getStringOrNull("language"),
            start = getString("start").toLocalDateTime(),
            end = getString("end").toLocalDateTime(),
            complexity = getStringOrNull("complexity"),
            feedbackId = getStringOrNull("feedbackId"),
            tags = getList<StringValue>("tags").map { it.get() },
            rooms = getList<StringValue>("rooms").map { it.get() },
            speakers = getList<StringValue>("speakers").map { it.get() }
        )
    }

    private fun DSession.toEntity(conf: String): Entity {
        return Entity.newBuilder(
            keyFactory.addAncestor(PathElement.of(KIND_CONF, conf))
                .setKind(KIND_SESSION)
                .newKey(id)
        )
            .set("type", type.toValue())
            .set("title", title.toValue(excludeFromIndex = true))
            .set("description", description.toValue(excludeFromIndex = true))
            .set("language", language.toValue())
            .set("start", start.toString().toValue())
            .set("end", end.toString().toValue())
            .set("complexity", complexity.toValue())
            .set("feedbackId", feedbackId.toValue())
            .set("tags", tags.toValue())
            .set("rooms", rooms.toValue())
            .set("speakers", speakers.toValue())
            .build()
    }

    private fun DRoom.toEntity(conf: String): Entity {
        return Entity.newBuilder(
            keyFactory.addAncestor(PathElement.of(KIND_CONF, conf))
                .setKind(KIND_ROOM)
                .newKey(id)
        )
            .set("name", name.toValue())
            .build()
    }

    private fun DVenue.toEntity(conf: String): Entity {
        return Entity.newBuilder(
            keyFactory.addAncestor(PathElement.of(KIND_CONF, conf))
                .setKind(KIND_VENUE)
                .newKey(id)
        )
            .set("name", name.toValue())
            .set("address", address.toValue())
            .set("latitude", latitude.toValue())
            .set("longitude", longitude.toValue())
            .set("description", description.toJsonElement().toString().toValue(true))
            .set("imageUrl", imageUrl.toValue())
            .build()
    }

    private fun List<DPartnerGroup>.toEntity(conf: String): Entity {
        return Entity.newBuilder(
            keyFactory.addAncestor(PathElement.of(KIND_CONF, conf))
                .setKind(KIND_PARTNERGROUPS)
                .newKey(THE_PARTNERGROUPS)
        )
            .set(THE_PARTNERGROUPS, map { it.toMap() }.toJsonElement().toString().toValue(excludeFromIndex = true))
            .build()
    }

    private fun DSpeaker.toEntity(conf: String): Entity {
        return Entity.newBuilder(
            keyFactory.addAncestor(PathElement.of(KIND_CONF, conf))
                .setKind(KIND_SPEAKER)
                .newKey(id)
        )
            .set("name", name.toValue())
            .set("bio", bio.toValue(excludeFromIndex = true))
            .set("company", company.toValue())
            .set("photoUrl", photoUrl.toValue())
            .set("companyLogoUrl", companyLogoUrl.toValue())
            .set("city", city.toValue())
            .set("links", links.map { it.toMap().toJsonElement().toString() }.toValue(excludeFromIndex = true))
            .build()
    }

    private fun DPartnerGroup.toMap(): Map<String, Any?> {
        return mapOf(
            "key" to key,
            "partners" to partners.map { it.toMap() }
        )
    }

    private fun DPartner.toMap(): Map<String, Any?> {
        return mapOf(
            "name" to this.name,
            "url" to this.url,
            "logoUrl" to this.logoUrl
        )
    }
    private fun Map<String, Any?>.toPartner(): DPartner {
        return DPartner(
            name = get("name").asString,
            url = get("url").asString,
            logoUrl = get("logoUrl").asString
        )
    }

    private fun Map<String, Any?>.toPartnerGroup(): DPartnerGroup {
        return DPartnerGroup(
            key = get("key").asString,
            partners = get("partners").asList.map { it.asMap.toPartner() },
        )
    }

    private fun DLink.toMap(): Map<String, Any?> {
        return mapOf(
            "key" to key,
            "url" to url
        )
    }

    private fun DConfig.toEntity(conf: String): Entity {
        return Entity.newBuilder(
            keyFactory.addAncestor(PathElement.of(KIND_CONF, conf))
                .setKind(KIND_CONFIG)
                .newKey(THE_CONFIG)
        )
            .set("timeZone", timeZone.toValue())
            .build()
    }

    private fun Entity.toConfig(): DConfig {
        return DConfig(
            name = getString("name"),
            timeZone = getString("timeZone")
        )
    }

    fun readRooms(conf: String): List<DRoom> {
        log("readRooms")
        val query: EntityQuery? = Query.newEntityQueryBuilder()
            .setKind(KIND_ROOM)
            .setLimit(100)
            .setFilter(
                StructuredQuery.PropertyFilter.hasAncestor(
                    keyFactory.setKind(KIND_CONF).newKey(conf)
                )
            )
            .build()
        val result = datastore.run(query)

        val items = mutableListOf<DRoom>()
        result.forEach {
            items.add(it.toRoom())
        }

        return items
    }

    fun readVenues(conf: String): List<DVenue> {
        val query: EntityQuery? = Query.newEntityQueryBuilder()
            .setKind(KIND_VENUE)
            .setLimit(100)
            .setFilter(
                StructuredQuery.PropertyFilter.hasAncestor(
                    keyFactory.setKind(KIND_CONF).newKey(conf)
                )
            )
            .build()
        val result = datastore.run(query)

        val items = mutableListOf<DVenue>()
        result.forEach {
            items.add(it.toVenue())
        }

        return items
    }

    fun readSpeakers(conf: String): List<DSpeaker> {
        log("readSpeakers")
        val query: EntityQuery? = Query.newEntityQueryBuilder()
            .setKind(KIND_SPEAKER)
            .setLimit(100)
            .setFilter(
                StructuredQuery.PropertyFilter.hasAncestor(
                    keyFactory.setKind(KIND_CONF).newKey(conf)
                )
            )
            .build()
        val result = datastore.run(query)

        val items = mutableListOf<DSpeaker>()
        result.forEach {
            items.add(it.toSpeaker())
        }

        return items
    }

    companion object {
        private fun Any?.toValue(excludeFromIndex: Boolean = false): Value<*> {
            return when (this) {
                is String -> StringValue.newBuilder(this).setExcludeFromIndexes(excludeFromIndex).build()
                is Int -> LongValue.newBuilder(this.toLong()).setExcludeFromIndexes(excludeFromIndex).build()
                is Double -> DoubleValue.newBuilder(this).setExcludeFromIndexes(excludeFromIndex).build()
                is List<*> -> ListValue.newBuilder().apply {
                    this@toValue.forEach { addValue(it.toValue(excludeFromIndex)) }
                }.build()
                null -> NullValue.newBuilder().setExcludeFromIndexes(excludeFromIndex).build()
                else -> error("unsupported value: $this")
            }
        }

        private fun Entity.getStringOrNull(name: String): String? = try {
            getString(name)
        } catch (_: Exception) {
            null
        }

        private fun Entity.getDoubleOrNull(name: String): Double? = try {
            getDouble(name)
        } catch (_: Exception) {
            null
        }


        private const val KIND_SESSION = "Session"
        private const val KIND_CONF = "Conf"
        private const val KIND_CONFIG = "Config"
        private const val KIND_ROOM = "Room"
        private const val KIND_SPEAKER = "Speaker"
        private const val KIND_PARTNERGROUPS = "Partners"
        private const val KIND_VENUE = "Venue"

        private const val THE_CONFIG = "config"
        private const val THE_PARTNERGROUPS = "partnerGroups"
    }
}