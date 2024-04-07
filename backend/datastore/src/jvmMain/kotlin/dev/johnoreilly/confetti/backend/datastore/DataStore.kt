package dev.johnoreilly.confetti.backend.datastore

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.BooleanValue
import com.google.cloud.datastore.Cursor
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.DatastoreReaderWriter
import com.google.cloud.datastore.DoubleValue
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.EntityQuery
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.KeyFactory
import com.google.cloud.datastore.ListValue
import com.google.cloud.datastore.LongValue
import com.google.cloud.datastore.NullValue
import com.google.cloud.datastore.PathElement
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.StringValue
import com.google.cloud.datastore.StructuredQuery
import com.google.cloud.datastore.Value
import com.google.datastore.v1.QueryResultBatch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.asList
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.asString
import net.mbonnin.bare.graphql.toAny
import net.mbonnin.bare.graphql.toJsonElement


fun googleCredentials(name: String): GoogleCredentials {
    return GoogleCredentials::class.java.classLoader.getResourceAsStream(name)?.use {
        GoogleCredentials.fromStream(it)
    } ?: error("no credentials found for $name")
}

internal fun initDatastore(): Datastore {
    return DatastoreOptions.newBuilder()
        .setCredentials(googleCredentials("gcp_service_account_key.json")).build().service
}

class DataStore {
    private val datastore: Datastore = initDatastore()

    private val keyFactory: KeyFactory
        get() {
            return datastore.newKeyFactory()
        }


    private fun readBookmarksEntity(uid: String, conference: String): Entity? {
        val key = keyFactory.addAncestor(PathElement.of(KIND_USER, uid))
            .setKind(KIND_BOOKMARKS)
            .newKey(conference)

        return try {
            datastore.get(key)
        } catch (e: Exception) {
            null
        }
    }

    fun readBookmarks(uid: String, conference: String): Set<String> {
        return readBookmarksEntity(uid, conference)?.names.orEmpty()
    }

    fun addBookmark(uid: String, conference: String, sessionId: String): Set<String> {
        var entityBuilder: Entity.Builder? = readBookmarksEntity(uid, conference)?.let {
            Entity.newBuilder(it)
        }
        if (entityBuilder == null) {
            val key = keyFactory.addAncestor(PathElement.of(KIND_USER, uid))
                .setKind(KIND_BOOKMARKS)
                .newKey(conference)

            entityBuilder = Entity.newBuilder(key)!!
        }
        entityBuilder.set(sessionId, BooleanValue.of(true))
        val newEntity = entityBuilder.build()
        datastore.runInTransaction {
            it.put(newEntity)
        }

        return newEntity.names
    }

    fun removeBookmark(uid: String, conference: String, sessionId: String): Set<String> {
        val entity = readBookmarksEntity(uid, conference)

        if (entity == null) {
            return emptySet()
        }

        if (!entity.contains(sessionId)) {
            return emptySet()
        }

        val newEntity = Entity.newBuilder(entity)
            .remove(sessionId)
            .build()
        datastore.runInTransaction {
            it.put(newEntity)
        }

        return newEntity.names
    }

    fun write(
        sessions: List<DSession>,
        rooms: List<DRoom>,
        speakers: List<DSpeaker>,
        partnerGroups: List<DPartnerGroup>,
        venues: List<DVenue>,
        config: DConfig
    ): Int {
        val conf = config.id

        val map = sessions.flatMap { session ->
            session.speakers.map {
                it to session.id
            }
        }.groupBy(
            { it.first },
            { it.second },
        )

        @Suppress("NAME_SHADOWING")
        val speakers = speakers.map {
            it.copy(
                sessions = map.get(it.id).orEmpty()
            )
        }
        datastore.runInTransaction {
            it.put(partnerGroups.toEntity(conf))
            val config2 = config.copy(
                days = sessions.map { it.start.date }.toSet().toList()
            )
            it.put(config2.toEntity(conf))
            it.deleteAll(conf, KIND_VENUE)
            it.write(venues.map { it.toEntity(conf) })
            it.deleteAll(conf, KIND_ROOM)
            it.write(rooms.map { it.toEntity(conf) })
            it.deleteAll(conf, KIND_SPEAKER)
            it.deleteAll(conf, KIND_SESSION)
        }

        // This is written outside a transaction because a transaction cannot write more than 500 entities at once
        datastore.write(sessions.map { it.toEntity(conf) })
        datastore.write(speakers.map { it.toEntity(conf) })

        return sessions.size
    }

    /**
     * DataStore cannot write more than 500 entities in a single call
     */
    private fun DatastoreReaderWriter.write(entities: List<Entity>) {
        entities.chunked(500).forEach {
            put(*it.toTypedArray())
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
        val result = run(query)

        val keys = mutableListOf<Key>()
        result.forEach {
            keys.add(it.key)
        }

        delete(*keys.toTypedArray())
    }

    private fun log(message: String) {
        if (false) {
            println(message)
        }
    }

    private fun eq(field: String, value: Any): StructuredQuery.PropertyFilter {
        return when (value) {
            is String -> StructuredQuery.PropertyFilter.eq(field, value)
            is Long -> StructuredQuery.PropertyFilter.eq(field, value)
            else -> TODO("$value")
        }
    }

    private fun ge(field: String, value: Any): StructuredQuery.PropertyFilter {
        return when (value) {
            is String -> StructuredQuery.PropertyFilter.ge(field, value)
            is Long -> StructuredQuery.PropertyFilter.ge(field, value)
            else -> TODO("$value")
        }
    }

    private fun le(field: String, value: Any): StructuredQuery.PropertyFilter {
        return when (value) {
            is String -> StructuredQuery.PropertyFilter.le(field, value)
            is Long -> StructuredQuery.PropertyFilter.le(field, value)
            else -> TODO("$value")
        }
    }

    private fun and(filters: List<StructuredQuery.PropertyFilter>): StructuredQuery.Filter {
        if (filters.size == 1) {
            return filters.get(0)
        } else {
            return StructuredQuery.CompositeFilter.and(
                filters.get(0),
                *filters.drop(1).toTypedArray()
            )
        }
    }

    fun readSessions(
        conf: String,
        ids: List<String>
    ): List<DSession> {
        val keys = ids.map { keyFactory.setKind(KIND_SESSION).addAncestor(PathElement.of(KIND_CONF, conf)).newKey(it) }

        return datastore.get(keys).map { it.toSession() }
    }

    fun readSessions(
        conf: String,
        limit: Int,
        cursor: String?,
        filters: List<DFilter>,
        orderBy: DOrderBy?
    ): DPage<DSession> {
        log("readSessions limit=$limit")

        val dFilters = filters.map {
            when (it.comparator) {
                DComparatorEq -> eq(it.field, it.value)
                DComparatorGe -> ge(it.field, it.value)
                DComparatorLe -> le(it.field, it.value)
            }
        } + StructuredQuery.PropertyFilter.hasAncestor(
            keyFactory.setKind(KIND_CONF).newKey(conf)
        )

        val query: EntityQuery? = Query.newEntityQueryBuilder()
            .setKind(KIND_SESSION)
            .setLimit(limit)
            .apply {
                if (cursor != null) {
                    setStartCursor(Cursor.fromUrlSafe(cursor))
                }
            }
            .setFilter(and(dFilters))
            .apply {
                if (orderBy != null) {
                    setOrderBy(
                        StructuredQuery.OrderBy(
                            orderBy.field,
                            orderBy.direction.toDirection()
                        )
                    )
                }
            }
            .build()
        val result = datastore.run(query)

        val items = mutableListOf<DSession>()
        result.forEach {
            items.add(it.toSession())
        }

        log("moreResults=${result.moreResults} cursor=${result.cursorAfter.toUrlSafe()}")
        val nextPageCursor = when (result.moreResults) {
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


    fun readConfigs(orderBy: DOrderBy): List<DConfig> {
        log("readConfig")
        val query: EntityQuery? = Query.newEntityQueryBuilder()
            .setKind(KIND_CONFIG)
            .setOrderBy(StructuredQuery.OrderBy(orderBy.field, orderBy.direction.toDirection()))
            .build()
        val result = datastore.run(query)

        val items = mutableListOf<DConfig>()
        result.forEach {
            items.add(it.toConfig())
        }

        return items
    }

    private fun DDirection.toDirection(): StructuredQuery.OrderBy.Direction = when (this) {
        DDirection.ASCENDING -> StructuredQuery.OrderBy.Direction.ASCENDING
        DDirection.DESCENDING -> StructuredQuery.OrderBy.Direction.DESCENDING
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
        @Suppress("UNCHECKED_CAST")
        return DVenue(
            id = key.name,
            name = getString("name"),
            address = getStringOrNull("address"),
            latitude = getDoubleOrNull("latitude"),
            longitude = getDoubleOrNull("longitude"),
            description = (Json.parseToJsonElement(getString("description"))
                .toAny() as Map<String, String>),
            imageUrl = getStringOrNull("imageUrl"),
            floorPlanUrl = getStringOrNull("floorPlanUrl"),
        )
    }

    private fun Entity.toSpeaker(): DSpeaker {
        return DSpeaker(
            id = key.name,
            name = getString("name"),
            bio = getStringOrNull("bio"),
            tagline = getStringOrNull("tagline"),
            company = getStringOrNull("company"),
            links = getListOrNull<StringValue>("links").orEmpty().map {
                Json.parseToJsonElement(it.get()).toAny().asMap.toLink()
            },
            photoUrl = getStringOrNull("photoUrl"),
            companyLogoUrl = getStringOrNull("companyLogoUrl"),
            city = getStringOrNull("city"),
            sessions = getListOrNull<StringValue>("sessions").orEmpty().map { it.get() }
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
            shortDescription = getStringOrNull("shortDescription"),
            language = getStringOrNull("language"),
            start = getString("start").toLocalDateTime(),
            end = getString("end").toLocalDateTime(),
            complexity = getStringOrNull("complexity"),
            feedbackId = getStringOrNull("feedbackId"),
            tags = getList<StringValue>("tags").map { it.get() },
            rooms = getList<StringValue>("rooms").map { it.get() },
            speakers = getList<StringValue>("speakers").map { it.get() },
            links = getListOrNull<StringValue>("links").orEmpty().map {
                Json.parseToJsonElement(it.get()).toAny().asMap.toLink()
            }
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
            .set(
                "links",
                links.map { it.toMap().toJsonElement().toString() }.toValue(excludeFromIndex = true)
            )
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
            .set("floorPlanUrl", floorPlanUrl.toValue())
            .build()
    }

    private fun List<DPartnerGroup>.toEntity(conf: String): Entity {
        return Entity.newBuilder(
            keyFactory.addAncestor(PathElement.of(KIND_CONF, conf))
                .setKind(KIND_PARTNERGROUPS)
                .newKey(THE_PARTNERGROUPS)
        )
            .set(
                THE_PARTNERGROUPS,
                map { it.toMap() }.toJsonElement().toString().toValue(excludeFromIndex = true)
            )
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
            .set("tagline", tagline.toValue(excludeFromIndex = true))
            .set("company", company.toValue())
            .set("photoUrl", photoUrl.toValue())
            .set("companyLogoUrl", companyLogoUrl.toValue())
            .set("city", city.toValue())
            .set(
                "links",
                links.map { it.toMap().toJsonElement().toString() }.toValue(excludeFromIndex = true)
            )
            .set("sessions", sessions.toValue())
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
            "logoUrl" to this.logoUrl,
            "logoUrlDark" to this.logoUrlDark,
        )
    }

    private fun Map<String, Any?>.toPartner(): DPartner {
        return DPartner(
            name = get("name").asString,
            url = get("url").asString,
            logoUrl = get("logoUrl").asString,
            logoUrlDark = get("logoUrlDark")?.asString,
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
            .set("name", name.toValue())
            .set("timeZone", timeZone.toValue())
            .set("days", days.map { it.toString() }.toValue())
            .set("themeColor", themeColor)
            .build()
    }

    private fun Entity.toConfig(): DConfig {
        return DConfig(
            id = key.ancestors.single { it.kind == KIND_CONF }.name,
            name = getStringOrNull("name") ?: "",
            timeZone = getString("timeZone"),
            days = getList<StringValue>("days").map { LocalDate.parse(it.get()) },
            themeColor = getStringOrNull("themeColor")
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
            .setLimit(500)
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

    fun updateDays() {
        ConferenceId.values().forEach { conferenceId ->
            val entity = datastore.get(
                keyFactory
                    .setKind(KIND_CONFIG)
                    .addAncestor(PathElement.of(KIND_CONF, conferenceId.id))
                    .newKey(THE_CONFIG)
            )

            datastore.put(Entity.newBuilder(entity).set(
                "days", when (conferenceId) {
                    ConferenceId.DroidConSF2022 -> listOf("2022-06-02", "2022-06-03")
                    ConferenceId.DevFestNantes2022 -> listOf("2022-10-20", "2022-10-21")
                    ConferenceId.FrenchKit2022 -> listOf("2022-09-29", "2022-09-30")
                    ConferenceId.GraphQLSummit2022 -> listOf("2022-10-04", "2022-10-05")
                    ConferenceId.DroidConLondon2022 -> listOf("2022-10-27", "2022-10-28")
                    ConferenceId.Fosdem2023 -> listOf("2023-02-04", "2023-02-05")
                    ConferenceId.KotlinConf2023 -> listOf("2023-04-12", "2023-04-14")
                    else -> error("Cannot update days for $conferenceId")
                }.map { StringValue(it) }
            ).build())
        }
    }

    fun updateSessions(block: (Entity) -> Entity?) {
        forEachSession {
            block(it)?.let { datastore.put(it) }
        }
    }

    fun forEachSession(block: (Entity) -> Unit) {
        val queryBuilder = Query.newEntityQueryBuilder()
            .setKind(KIND_SESSION)
            .setLimit(50)

        while (true) {
            val result = datastore.run(queryBuilder.build())
            result.forEach {
                block(it)
            }

            when (result.moreResults) {
                QueryResultBatch.MoreResultsType.MORE_RESULTS_TYPE_UNSPECIFIED -> TODO()
                QueryResultBatch.MoreResultsType.NOT_FINISHED -> queryBuilder.setStartCursor(result.cursorAfter)
                QueryResultBatch.MoreResultsType.MORE_RESULTS_AFTER_LIMIT -> queryBuilder.setStartCursor(
                    result.cursorAfter
                )

                QueryResultBatch.MoreResultsType.MORE_RESULTS_AFTER_CURSOR -> queryBuilder.setStartCursor(
                    result.cursorAfter
                )

                QueryResultBatch.MoreResultsType.NO_MORE_RESULTS -> break
                QueryResultBatch.MoreResultsType.UNRECOGNIZED -> TODO()
                null -> TODO()
            }
        }
    }

    fun updateSpeakers(block: (Entity) -> Entity) {
        val queryBuilder = Query.newEntityQueryBuilder()
            .setKind(KIND_SPEAKER)
            .setLimit(50)

        while (true) {
            val result = datastore.run(queryBuilder.build())
            val newEntities = buildList {
                result.forEach {
                    this.add(block(it))
                }
            }
            println("putting ${newEntities.size} entities")
            datastore.put(*newEntities.toTypedArray())

            when (result.moreResults) {
                QueryResultBatch.MoreResultsType.MORE_RESULTS_TYPE_UNSPECIFIED -> TODO()
                QueryResultBatch.MoreResultsType.NOT_FINISHED -> queryBuilder.setStartCursor(result.cursorAfter)
                QueryResultBatch.MoreResultsType.MORE_RESULTS_AFTER_LIMIT -> queryBuilder.setStartCursor(
                    result.cursorAfter
                )

                QueryResultBatch.MoreResultsType.MORE_RESULTS_AFTER_CURSOR -> queryBuilder.setStartCursor(
                    result.cursorAfter
                )

                QueryResultBatch.MoreResultsType.NO_MORE_RESULTS -> break
                QueryResultBatch.MoreResultsType.UNRECOGNIZED -> TODO()
                null -> TODO()
            }
        }
    }

    fun readSpeaker(conf: String, id: String): DSpeaker {
        return datastore.get(keyFactory.setKind(KIND_SPEAKER).addAncestor(PathElement.of(KIND_CONF, conf)).newKey(id))
            .toSpeaker()
    }

    fun removeBookmarks(uid: String?) {
        val query = Query.newKeyQueryBuilder()
            .setKind(KIND_BOOKMARKS)
            .setLimit(100)
            .setFilter(
                StructuredQuery.PropertyFilter.hasAncestor(
                    keyFactory.setKind(KIND_USER).newKey(uid)
                )
            )
            .build()

        while (true) {
            val result = datastore.run(query)

            val keys = result.asSequence().toList().toTypedArray()
            datastore.delete(*keys)

            if (keys.isEmpty() || result.moreResults == QueryResultBatch.MoreResultsType.NO_MORE_RESULTS) {
                break
            }
        }
    }

    companion object {
        fun <T, R> Iterator<T>.map(block: (T) -> R) = buildList<R> {
            this@map.forEach {
                add(block(it))
            }
        }

        fun Any?.toValue(excludeFromIndex: Boolean = false): Value<*> {
            return when (this) {
                is String -> StringValue.newBuilder(this).setExcludeFromIndexes(excludeFromIndex)
                    .build()

                is Int -> LongValue.newBuilder(this.toLong())
                    .setExcludeFromIndexes(excludeFromIndex).build()

                is Double -> DoubleValue.newBuilder(this).setExcludeFromIndexes(excludeFromIndex)
                    .build()

                is List<*> -> ListValue.newBuilder().apply {
                    this@toValue.forEach { addValue(it.toValue(excludeFromIndex)) }
                }.build()

                null -> NullValue.newBuilder().setExcludeFromIndexes(excludeFromIndex).build()
                else -> error("unsupported value: $this")
            }
        }

        fun Entity.getStringOrNull(name: String): String? = try {
            getString(name)
        } catch (_: Exception) {
            null
        }

        fun <T : Value<*>> Entity.getListOrNull(name: String): List<T>? = try {
            getList<T>(name)
        } catch (_: Exception) {
            null
        }

        fun Entity.getDoubleOrNull(name: String): Double? = try {
            getDouble(name)
        } catch (_: Exception) {
            null
        }


        internal const val KIND_SESSION = "Session"
        internal const val KIND_CONF = "Conf"
        internal const val KIND_USER = "User"
        internal const val KIND_CONFIG = "Config"
        internal const val KIND_ROOM = "Room"
        internal const val KIND_SPEAKER = "Speaker"
        internal const val KIND_PARTNERGROUPS = "Partners"
        internal const val KIND_BOOKMARKS = "Bookmarks"
        internal const val KIND_VENUE = "Venue"

        internal const val THE_CONFIG = "config"
        internal const val THE_PARTNERGROUPS = "partnerGroups"
    }
}
