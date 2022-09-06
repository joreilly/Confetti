package dev.johnoreilly.confetti.backend.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDirective
import com.expediagroup.graphql.server.operations.Query
import dev.johnoreilly.confetti.backend.DefaultApplication.Companion.SOURCE_KEY
import graphql.introspection.Introspection
import graphql.schema.DataFetchingEnvironment
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component

@GraphQLDirective(
    name = "requiresOptIn",
    description = "This field can be changed without warning",
    locations = [Introspection.DirectiveLocation.FIELD_DEFINITION]
)
annotation class RequiresOptIn(val feature: String)

@Component
class RootQuery() : Query {
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
        return dfe.source().venue(id)
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

    fun config(dfe: DataFetchingEnvironment): Configuration {
        return dfe.source().configuration()
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
 * @param language an [IETF language code](https://en.wikipedia.org/wiki/IETF_language_tag) like en-US
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
    val socials: List<Social>,
    val photoUrl: String?,
)

data class Social(
    val icon: String?,
    val link: String,
    val name: String,
)

data class PartnerGroup(
    val title: String,
    val partners: List<Partner>,
)

data class Partner(
    val name: String,
    val logoUrl: String,
    val url: String,
)

data class Venue(
    val name: String,
    val address: String? = null,
    val coordinates: String? = null,
    val description: String,
    val descriptionFr: String,
    val imageUrl: String,
)

data class Configuration(
    val timezone: String,
)
