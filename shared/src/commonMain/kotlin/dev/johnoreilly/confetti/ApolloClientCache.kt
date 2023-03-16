package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import dev.johnoreilly.confetti.di.getDatabaseName
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface TokenProvider {
    suspend fun token(forceRefresh: Boolean): String?
}

class ApolloClientCache : KoinComponent {
    val _clients = mutableMapOf<String, ApolloClient>()
    val mutex = Mutex(false)
    private val tokenProvider = get<TokenProvider>()

    private val httpInterceptor = object : HttpInterceptor {
        override suspend fun intercept(
            request: HttpRequest,
            chain: HttpInterceptorChain
        ): HttpResponse {
            val token = tokenProvider.token(false)
            if (token == null) {
                return chain.proceed(request)
            }
            val newRequest = request.newBuilder().addHeader("Authorization", "Bearer $token").build()
            return chain.proceed(newRequest)
        }

    }
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
            .serverUrl("https://confetti-app.dev/graphql")
            .addHttpHeader("conference", conference)
            .addHttpInterceptor(httpInterceptor)
            .autoPersistedQueries()
            .normalizedCache(
                memoryFirstThenSqlCacheFactory,
                writeToCacheAsynchronously = writeToCacheAsynchronously
            )
            .build()
    }

    fun close() {
        _clients.values.forEach {
            it.close()
        }
        _clients.clear()
    }

    fun clear() {
        _clients.values.forEach {
            it.apolloStore.clearAll()
            it.close()
        }
        _clients.clear()
    }
}