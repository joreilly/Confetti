package dev.johnoreilly.confetti.backend.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLDirective
import com.expediagroup.graphql.server.operations.Query
import dev.johnoreilly.confetti.backend.DefaultApplication.Companion.KEY_SOURCE
import dev.johnoreilly.confetti.backend.datastore.DDirection
import dev.johnoreilly.confetti.backend.datastore.DOrderBy
import dev.johnoreilly.confetti.backend.datastore.DataStore
import graphql.introspection.Introspection
import graphql.schema.DataFetchingEnvironment
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.springframework.stereotype.Component

@GraphQLDirective(
    name = "requiresOptIn",
    description = "This field can be changed without warning",
    locations = [Introspection.DirectiveLocation.FIELD_DEFINITION]
)
annotation class RequiresOptIn(val feature: String)

@Component
class RootMutation : Query {
    fun addBookmark(dfe: DataFetchingEnvironment, sessionId: String): Boolean {
        dfe.source().addBookmark(sessionId)
        return true
    }

    fun removeBookmark(dfe: DataFetchingEnvironment, sessionId: String): Boolean {
        return dfe.source().removeBookmark(sessionId)
    }
}

@Component
class RootQuery : Query {
    fun rooms(dfe: DataFetchingEnvironment): List<Room> {
        return dfe.source().rooms()
    }

    fun sessions(
        dfe: DataFetchingEnvironment,
        first: Int? = 10,
        after: String? = null,
        filter: SessionFilter? = null,
        orderBy: SessionOrderBy? = SessionOrderBy(field = SessionField.STARTS_AT, direction = OrderByDirection.ASCENDING)
    ): SessionConnection {
        return dfe.source().sessions(
            first ?: 10,
            after,
            filter,
            orderBy
        )
    }

    fun speakers(dfe: DataFetchingEnvironment): List<Speaker> {
        return dfe.source().speakers()
    }

    fun venue(dfe: DataFetchingEnvironment, id: String): Venue {
        return dfe.source().venues().first { it.id == id }
    }

    fun venues(dfe: DataFetchingEnvironment): List<Venue> {
        return dfe.source().venues()
    }

    fun partnerGroups(dfe: DataFetchingEnvironment): List<PartnerGroup> {
        return dfe.source().partnerGroups()
    }

    fun session(dfe: DataFetchingEnvironment, id: String): Session {
        val nodes = dfe.source().sessions(100, after = null, null, null)
            .nodes

        return nodes.firstOrNull { it.id == id }
            ?: error("Cannot find id '$id' in ${nodes.size} nodes")
    }

    fun config(dfe: DataFetchingEnvironment): Conference {
        return dfe.source().conference()
    }

    fun bookmarks(dfe: DataFetchingEnvironment): List<String> {
        return dfe.source().bookmarks().toList()
    }

    fun conferences(orderBy: ConferenceOrderBy? = null): List<Conference> {
        val orderBy =
            orderBy ?: ConferenceOrderBy(ConferenceField.DAYS, OrderByDirection.DESCENDING)
        return DataStore().readConfigs(
            DOrderBy(orderBy.field.value, orderBy.direction.toDDirection())
        ).map {
            it.toConference()
        }
    }


}

internal fun OrderByDirection.toDDirection(): DDirection {
    return when (this) {
        OrderByDirection.ASCENDING -> DDirection.ASCENDING
        OrderByDirection.DESCENDING -> DDirection.DESCENDING
    }
}

class LocalDateTimeFilter(
    val before: LocalDateTime? = null,
    val after: LocalDateTime? = null,
)
class SessionFilter(
    val startsAt: LocalDateTimeFilter? = null,
    val endsAt: LocalDateTimeFilter? = null,
)

class SessionOrderBy(
    val field: SessionField,
    val direction: OrderByDirection
)

enum class SessionField(val value: String) {
    STARTS_AT("start"),
}

class ConferenceOrderBy(
    val field: ConferenceField,
    val direction: OrderByDirection
)

enum class OrderByDirection {
    ASCENDING,
    DESCENDING
}

enum class ConferenceField(val value: String) {
    DAYS("days"),
}

private fun DataFetchingEnvironment.source(): DataSource {
    return graphQlContext.get(KEY_SOURCE)
}

data class Room(
    val id: String,
    val name: String,
    val capacity: Int?,
)

data class SessionConnection(
    val nodes: List<Session>,
    val pageInfo: PageInfo,
)


data class PageInfo(
    val endCursor: String?,
)

/**
 */
data class Session(
    val id: String,
    val title: String,
    val description: String?,
    @GraphQLDescription("""A shorter version of description for use when real estate is scarce like watches for an example.
This field might have the same value as description if a shortDescription is not available""")
    val shortDescription: String?,
    @GraphQLDescription("""An [IETF language code](https://en.wikipedia.org/wiki/IETF_language_tag) like en-US""")
    val language: String?,
    private val speakerIds: Set<String>,
    val tags: List<String>,
    @Deprecated("use startsAt instead")
    val startInstant: Instant,
    @Deprecated("use endsAt instead")
    val endInstant: Instant,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    private val roomIds: Set<String>,
    val complexity: String?,
    val feedbackId: String?,
    @GraphQLDescription("""One of "break", "lunch", "party", "keynote", "talk" or any other conference-specific format""")
    val type: String,
) {
    fun speakers(dfe: DataFetchingEnvironment): List<Speaker> {
        return dfe.source().speakers().filter {
            speakerIds.contains(it.id)
        }
    }

    @Deprecated("use rooms instead")
    fun room(dfe: DataFetchingEnvironment): Room? {
        val roomId = roomIds.firstOrNull()
        if (roomId == null) {
            return null
        }
        return dfe.source().rooms().firstOrNull {
            it.id == roomId
        }
    }

    fun rooms(dfe: DataFetchingEnvironment): List<Room> {
        return dfe.source().rooms().filter {
            roomIds.contains(it.id)
        }
    }
}

data class Speaker(
    val id: String,
    val name: String,
    val bio: String?,
    val company: String?,
    val companyLogoUrl: String?,
    val city: String?,
    val socials: List<Social>,
    val photoUrl: String?,
)


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

data class PartnerGroup(
    val title: String,
    val partners: List<Partner>,
)

data class Partner(
    val name: String,
    val logoUrl: String,
    val url: String,
)

/**
 * @property floorPlanUrl the url to an image containing the floor plan
 */
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

    @Deprecated("description(language: \"fr\") instead")
    val descriptionFr: String
        get() {
            return descriptions.get("fr") ?: descriptions.get("en") ?: ""
        }

    fun description(language: String? = "en"): String {
        return descriptions.get(language) ?: descriptions.get("en") ?: ""
    }
}

data class Conference(
    val id: String,
    val name: String,
    val timezone: String,
    val days: List<LocalDate>
)
