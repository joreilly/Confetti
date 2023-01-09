package dev.johnoreilly.confetti.backend.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDirective
import com.expediagroup.graphql.server.operations.Query
import dev.johnoreilly.confetti.backend.DefaultApplication.Companion.SOURCE_KEY
import dev.johnoreilly.confetti.backend.datastore.DataStore
import graphql.introspection.Introspection
import graphql.schema.DataFetchingEnvironment
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import dev.johnoreilly.confetti.backend.datastore.ConferenceId as Conference1

@GraphQLDirective(
    name = "requiresOptIn",
    description = "This field can be changed without warning",
    locations = [Introspection.DirectiveLocation.FIELD_DEFINITION]
)
annotation class RequiresOptIn(val feature: String)

@Component
class RootQuery : Query {
    fun rooms(dfe: DataFetchingEnvironment): List<Room> {
        return dfe.source().rooms()
    }

    fun sessions(dfe: DataFetchingEnvironment, first: Int? = 10, after: String? = null): SessionConnection {
        return dfe.source().sessions(first ?: 10, after)
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
        val nodes = dfe.source().sessions(100, after = null)
            .nodes

        return nodes.firstOrNull { it.id == id }
            ?: error("Cannot find id '$id' in ${nodes.size} nodes")
    }

    fun config(dfe: DataFetchingEnvironment): Conference {
        return dfe.source().conference()
    }

    fun conferences(): List<Conference> {
        return DataStore().readConfigs().map {
            it.toConference()
        }
    }
}

private fun DataFetchingEnvironment.source(): DataSource {
    return graphQlContext.get(SOURCE_KEY)
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
 * @property language an [IETF language code](https://en.wikipedia.org/wiki/IETF_language_tag) like en-US
 * @property type one of "break", "lunch", "party", "keynote", "talk" or any other conference-specific format
 */
data class Session(
    val id: String,
    val title: String,
    val description: String?,
    val language: String?,
    private val speakerIds: Set<String>,
    val tags: List<String>,
    @RequiresOptIn("experimental")
    val startInstant: Instant,
    @RequiresOptIn("experimental")
    val endInstant: Instant,
    private val roomIds: Set<String>,
    val complexity: String?,
    val feedbackId: String?,
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
    @Deprecated("use latitude and longitude instead")
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
)
