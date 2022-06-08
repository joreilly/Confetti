package fr.androidmakers.server

import com.expediagroup.graphql.server.operations.Query
import fr.androidmakers.server.model.*
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Component

enum class Conference {
  DroidConfSf,
  DevFestLille,
}


@Component
class RootQuery : Query {
  fun source(conference: Conference, dfe: DataFetchingEnvironment): Source {
    val dataSource = when(conference) {
      Conference.DroidConfSf ->  DroidConSfoDataSource()
      Conference.DevFestLille -> DevFestLilleDataSource()
    }

    dfe.graphQlContext.put(DATA_SOURCE_CONTEXT_KEY, dataSource)

    return Source(dataSource)
  }
}

class Source(private val dataSource: DataSource) {

  fun rooms(): List<Room> {
    return dataSource.rooms()
  }
  fun sessions(first: Int? = 10, after: String? = null): SessionConnection {
    return dataSource.sessions(first ?: 10, after)
  }
  fun speakers(): List<Speaker> {
    return dataSource.speakers()
  }

  fun venue(id: String): Venue {
    return dataSource.venue(id)
  }

  fun partnerGroups(): List<PartnerGroup> {
    return dataSource.partners()
  }

  fun session(id: String): Session {
    return dataSource.allSessions().first { it.id == id }
  }

  fun config(): Configuration {
    return Configuration(
        timezone = "Europe/Paris"
    )
  }
}
