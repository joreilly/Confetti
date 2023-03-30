package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.*
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import kotlinx.coroutines.flow.*
























class ConfettiRepository {
    val conferenceName = "KotlinConf 2023"

    // Create Apollo client with normalized cache
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("http://localhost:8080/graphql")
        .normalizedCache(MemoryCacheFactory(10_000_000).chain(SqlNormalizedCacheFactory(getDatabaseName(conferenceName))))
        .build()

    // Gets list of sessions from backend and then observes the cache for any changes
    val sessions = apolloClient.query(GetSessionsQuery()).watch().map {
        it.dataAssertNoErrors.sessions.map { it.sessionDetails }
    }

}