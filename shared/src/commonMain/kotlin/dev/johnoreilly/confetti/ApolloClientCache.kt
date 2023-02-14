package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import dev.johnoreilly.confetti.di.getDatabaseName
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ApolloClientCache : KoinComponent {
    val _clients = mutableMapOf<String, ApolloClient>()
    val mutex = Mutex(false)

    suspend fun getClient(conference: String): ApolloClient {
        return mutex.withLock {
            _clients.getOrPut(conference) {
                clientFor(conference, conference != "all")
            }
        }
    }

    private fun clientFor(
        conference: String,
        writeToCacheAsynchronously: Boolean
    ): ApolloClient {
        val sqlNormalizedCacheFactory = SqlNormalizedCacheFactory(getDatabaseName(conference))
        val memoryFirstThenSqlCacheFactory = MemoryCacheFactory(10 * 1024 * 1024)
            .chain(sqlNormalizedCacheFactory)

        return get<ApolloClient.Builder>()
            .serverUrl("https://graphql-dot-confetti-349319.uw.r.appspot.com/graphql?conference=$conference")
            //.serverUrl("http://10.0.2.2:8080/graphql?conference=graphqlsummit2022")
            .normalizedCache(
                memoryFirstThenSqlCacheFactory,
                writeToCacheAsynchronously = writeToCacheAsynchronously
            )
            .build()
    }
}