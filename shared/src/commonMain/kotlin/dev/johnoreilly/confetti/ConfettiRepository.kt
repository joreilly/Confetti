package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.*
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime

expect fun getDatabaseName(conference: String): String


internal class ConfettiRepository  {
    val conferenceName = "droidconlondon2022"

    private val apolloClient: ApolloClient = createApolloClient(conferenceName)

    val sessions = apolloClient.query(GetSessionsQuery()).watch().map {
        it.dataAssertNoErrors.sessions.nodes.map { it.sessionDetails }.sortedBy { it.startInstant }
    }

    val sessionsByDateMap: Flow<Map<LocalDate, List<SessionDetails>>> = sessions.map {
        it.groupBy { it.startInstant.toLocalDateTime(currentSystemDefault()).date }
    }



    private fun createApolloClient(conference: String): ApolloClient {
        val memoryFirstThenSqlCacheFactory = MemoryCacheFactory(10 * 1024 * 1024)
            .chain(SqlNormalizedCacheFactory(getDatabaseName(conference)))

        return ApolloClient.Builder()
            .serverUrl("https://graphql-dot-confetti-349319.uw.r.appspot.com/graphql?conference=$conference")
            .normalizedCache(memoryFirstThenSqlCacheFactory, writeToCacheAsynchronously = true)
            .build()
    }

}