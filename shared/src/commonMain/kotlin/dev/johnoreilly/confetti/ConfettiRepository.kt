package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.*
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime

class ConfettiRepository {
    val conferenceName = "kotlinconf2023"

    private val apolloClient = ApolloClient.Builder()
        //.serverUrl("http://10.0.2.2:8080/graphql?conference=$conferenceName")
        .serverUrl("https://kotlinconfetti.ew.r.appspot.com/graphql?conference=$conferenceName")
        .normalizedCache(MemoryCacheFactory(10 * 1024 * 1024).chain(SqlNormalizedCacheFactory(getDatabaseName(conferenceName))))
        .build()

    // Gets list of sessions from backend and then observe the cache for any changes
    val sessions = apolloClient.query(GetSessionsQuery()).watch().map {
        it.dataAssertNoErrors.sessions.map { it.sessionDetails }.sortedBy { it.start }
    }

    // Group sessions by date
    val sessionsByDateMap: Flow<Map<LocalDate, List<SessionDetails>>> = sessions.map {
        it.groupBy { it.start.date }
    }


}