package dev.johnoreilly.confetti.backend.graphql

import com.apollographql.apollo3.adapter.KotlinxInstantAdapter
import com.apollographql.apollo3.adapter.KotlinxLocalDateAdapter
import com.apollographql.apollo3.adapter.KotlinxLocalDateTimeAdapter
import com.apollographql.apollo3.annotations.ApolloAdapter
import com.apollographql.apollo3.annotations.GraphQLName
import com.apollographql.apollo3.annotations.ApolloObject
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.ExecutionContext
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.getOrElse
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import confetti.type.*
import confetti.type.ConferenceField
import confetti.type.ConferenceOrderByInput
import confetti.type.LinkType
import confetti.type.OrderByDirection
import confetti.type.SessionField
import confetti.type.SessionFilterInput
import confetti.type.SessionOrderByInput
import dev.johnoreilly.confetti.backend.Source
import dev.johnoreilly.confetti.backend.UserId
import dev.johnoreilly.confetti.backend.datastore.DOrderBy
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

annotation class RequiresOptIn(val feature: String)

@ApolloObject
@GraphQLName(name = "Mutation")
internal class RootMutation {
    fun addBookmark(executionContext: ExecutionContext, sessionId: String): Bookmarks {
        return Bookmarks(executionContext.source().addBookmark(sessionId).toList())
    }

    fun removeBookmark(executionContext: ExecutionContext, sessionId: String): Bookmarks {
        return Bookmarks(executionContext.source().removeBookmark(sessionId).toList())
    }
}

@ApolloAdapter
@GraphQLName(name = "Instant")
typealias MyInstantAdapter = KotlinxInstantAdapter

@ApolloAdapter
@GraphQLName(name = "LocalDate")
typealias MyLocalDateAdapter = KotlinxLocalDateAdapter


@ApolloAdapter
@GraphQLName(name = "LocalDateTime")
object MyLocalDateTimeAdapter : Adapter<LocalDateTime> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): LocalDateTime {
        return KotlinxLocalDateTimeAdapter.fromJson(reader, customScalarAdapters)
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: LocalDateTime) {
        KotlinxLocalDateTimeAdapter.toJson(writer, customScalarAdapters, value)
    }

}

@ApolloObject
@GraphQLName(name = "Query")
internal class RootQuery {
    fun rooms(executionContext: ExecutionContext): List<Room> {
        return executionContext.source().rooms()
    }

    fun sessions(
        executionContext: ExecutionContext,
        first: Int,
        after: Optional<String?>,
        filter: Optional<SessionFilterInput?>,
        orderBy: Optional<SessionOrderByInput?>
    ): SessionConnection {
        val orderBy = orderBy.getOrElse(SessionOrderByInput(OrderByDirection.ASCENDING, SessionField.STARTS_AT))
        return executionContext.source().sessions(
            first,
            after.getOrElse(null),
            filter.getOrElse(null),
            orderBy
        )
    }

    @Deprecated("Use speakersPage instead")
    fun speakers(executionContext: ExecutionContext): List<Speaker> {
        return executionContext.source().speakers(first = 100, after = null).nodes
    }

    fun speakersPage(
        executionContext: ExecutionContext,
        first: Int?,
        after: Optional<String?>,
    ): SpeakerConnection {
        return executionContext.source().speakers(first ?: 10, after.getOrNull())
    }

    fun speaker(executionContext: ExecutionContext, id: String): Speaker {
        return executionContext.source().speaker(id)
    }

    fun venue(executionContext: ExecutionContext, id: String): Venue {
        return executionContext.source().venues().first { it.id == id }
    }

    fun venues(executionContext: ExecutionContext): List<Venue> {
        return executionContext.source().venues()
    }

    fun partnerGroups(executionContext: ExecutionContext): List<PartnerGroup> {
        return executionContext.source().partnerGroups()
    }

    fun session(executionContext: ExecutionContext, id: String): Session {
        val nodes = executionContext.source().sessions(100, after = null, null, null)
            .nodes

        return nodes.firstOrNull { it.id == id }
            ?: error("Cannot find id '$id' in ${nodes.size} nodes")
    }

    fun config(executionContext: ExecutionContext): Conference {
        return executionContext.source().conference()
    }

    fun bookmarks(executionContext: ExecutionContext): Bookmarks? {
        if (executionContext.uid() == null) {
            return null
        }
        return Bookmarks(executionContext.source().bookmarks().toList())
    }

    fun bookmarkConnection(executionContext: ExecutionContext): BookmarkConnection? {
        if (executionContext.uid() == null) {
            return null
        }
        return BookmarkConnection(
            nodes = executionContext.source().sessions(executionContext.source().bookmarks().toList())
        )
    }

    fun conferences(orderBy: Optional<ConferenceOrderByInput?>): List<Conference> {
        val default = ConferenceOrderByInput(direction = OrderByDirection.DESCENDING, field = ConferenceField.DAYS)
        val orderBy1 =
            orderBy.getOrElse(default) ?: default
        return DataStore().readConfigs(
            DOrderBy(orderBy1.field.actualFieldName(), orderBy1.direction.toDDirection())
        ).map {
            it.toConference()
        }
    }
}

internal fun ConferenceField.actualFieldName(): String = when (this) {
    ConferenceField.DAYS -> "days"
}

@ApolloObject
internal class BookmarkConnection(
    val nodes: List<Session>
)

@ApolloObject
internal class Bookmarks(val sessionIds: List<String>) {
    val id = "Bookmarks"
}


fun ExecutionContext.uid(): String? {
    return this[UserId]?.uid
}
private fun ExecutionContext.source(): DataSource {
    return this[Source]!!.source
}

@ApolloObject
internal data class Room(
    val id: String,
    val name: String,
    val capacity: Int?,
)

@ApolloObject
internal data class SessionConnection(
    val nodes: List<Session>,
    val pageInfo: PageInfo,
)

@ApolloObject
internal data class PageInfo(
    val endCursor: String?,
)

@ApolloObject
internal data class Link(
    val type: LinkType,
    val url: String,
)

/**
 */
@ApolloObject
internal data class Session(
    val id: String,
    val title: String,
    val description: String?,
    val shortDescription: String?,
    val language: String?,
    private val speakerIds: Set<String>,
    val tags: List<String>,
    val startInstant: Instant,
    val endInstant: Instant,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    private val roomIds: Set<String>,
    val complexity: String?,
    val feedbackId: String?,
    val type: String,
    val links: List<Link>
) {
    fun speakers(executionContext: ExecutionContext): List<Speaker> {
        return executionContext.source().speakers(speakerIds.toList())
    }

    fun room(executionContext: ExecutionContext): Room? {
        val roomId = roomIds.firstOrNull()
        if (roomId == null) {
            return null
        }
        return executionContext.source().rooms().firstOrNull {
            it.id == roomId
        }
    }

    fun rooms(executionContext: ExecutionContext): List<Room> {
        return executionContext.source().rooms().filter {
            roomIds.contains(it.id)
        }
    }
}

@ApolloObject
internal data class SpeakerConnection(
    val nodes: List<Speaker>,
    val pageInfo: PageInfo,
)

@ApolloObject
internal data class Speaker(
    val id: String,
    val name: String,
    val bio: String?,
    val tagline: String?,
    val company: String?,
    val companyLogoUrl: String?,
    val city: String?,
    val socials: List<Social>,
    val photoUrl: String?,
    private val sessionIds: List<String>,
) {
    fun sessions(
        executionContext: ExecutionContext,
    ): List<Session> {
        return executionContext.source().sessions(
            sessionIds
        )
    }
}

@ApolloObject
data class Social(
    val icon: String?,
    @Deprecated("use url instead", ReplaceWith("url"))
    val link: String,
    val name: String,
) {
    @Suppress("DEPRECATION")
    val url: String
        get() = link
}

@ApolloObject
data class PartnerGroup(
    val title: String,
    val partners: List<Partner>,
)

@ApolloObject
data class Partner(
    val name: String,
    val logoUrl: String,
    val url: String,
)

/**
 * @property floorPlanUrl the url to an image containing the floor plan
 */
@ApolloObject
data class Venue(
    val id: String,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String? = null,
    val imageUrl: String?,
    val floorPlanUrl: String?,
    private val descriptions: Map<String, String>
) {
    @Deprecated(
        "use latitude and " +
            "longitude instead"
    )
    val coordinates: String?
        get() {
            return if (latitude != null && longitude != null) {
                "$latitude,$longitude"
            } else {
                null
            }
        }

    @Deprecated("use description(language: \"fr\") instead")
    val descriptionFr: String
        get() {
            return descriptions.get("fr") ?: descriptions.get("en") ?: ""
        }

    fun description(language: Optional<String?>): String {
        val lang = language.getOrElse("en") ?: "en"
        return descriptions.get(lang) ?: descriptions.get("en") ?: ""
    }
}

@ApolloObject
data class Conference(
    val id: String,
    val name: String,
    val timezone: String,
    val days: List<LocalDate>
)
