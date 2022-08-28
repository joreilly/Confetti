package dev.johnoreilly.confetti.backend.datastore

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.datastore.*
import com.google.datastore.v1.QueryResultBatch
import dev.johnoreilly.confetti.backend.datastore.DataStore.Companion.toValue
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.asString
import net.mbonnin.bare.graphql.toAny
import net.mbonnin.bare.graphql.toJsonElement
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
        config: DConfig
    ) {
        datastore.runInTransaction {
            it.put(*sessions.map { it.toEntity(conf) }.toTypedArray())
            it.put(*rooms.map { it.toEntity(conf) }.toTypedArray())
            it.put(*speakers.map { it.toEntity(conf) }.toTypedArray())
            it.put(config.toEntity(conf))
        }
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

    private fun Entity.toSpeaker(): DSpeaker {
        return DSpeaker(
            id = key.name,
            name = getString("name"),
            bio = getStringOrNull("bio"),
            company = getStringOrNull("company"),
            links = getList<StringValue>("links").map {
                Json.parseToJsonElement(it.get()).toAny().asMap.toLink()
            },
            photoUrl = getStringOrNull("photoUrl")
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
            .set("links", links.map { it.toMap().toJsonElement().toString() }.toValue(excludeFromIndex = true))
            .build()
    }

    private fun DLink.toMap(): Map<String, Any> {
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
        return DConfig(getString("timeZone"))
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

        private const val KIND_SESSION = "Session"
        private const val KIND_CONF = "Conf"
        private const val KIND_CONFIG = "Config"
        private const val KIND_ROOM = "Room"
        private const val KIND_SPEAKER = "Speaker"

        private const val THE_CONFIG = "config"
    }
}