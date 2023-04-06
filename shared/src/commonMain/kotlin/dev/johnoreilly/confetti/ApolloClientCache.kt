package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloRequest
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.ExecutionContext
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo3.interceptor.ApolloInterceptor
import com.apollographql.apollo3.interceptor.ApolloInterceptorChain
import dev.johnoreilly.confetti.di.getDatabaseName
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface TokenProvider {
    suspend fun token(forceRefresh: Boolean): String?
}


class TokenProviderContext(val tokenProvider: TokenProvider) : ExecutionContext.Element {
    override val key: ExecutionContext.Key<*>
        get() = Key

    companion object Key : ExecutionContext.Key<TokenProviderContext>
}

class ApolloClientCache : KoinComponent {
    val _clients = mutableMapOf<String, ApolloClient>()
    val mutex = reentrantLock()

    private val tokenProviderInterceptor = object : ApolloInterceptor {
        override fun <D : Operation.Data> intercept(
            request: ApolloRequest<D>,
            chain: ApolloInterceptorChain
        ): Flow<ApolloResponse<D>> {
            val tokenProvider = request.executionContext[TokenProviderContext]?.tokenProvider
            if (tokenProvider == null) {
                return chain.proceed(request)
            }
            val token = runBlocking {
                tokenProvider.token(false)
            }
            if (token == null) {
                return chain.proceed(request)
            }
            val newRequest =
                request.newBuilder().addHttpHeader("Authorization", "Bearer $token").build()
            return chain.proceed(newRequest)
        }
    }

    suspend fun getClient(conference: String, uid: String?): ApolloClient {
        return mutex.withLock {
            _clients.getOrPut("$conference-$uid") {
                clientFor(conference, uid, true)
            }
        }
    }

    fun getClient(conference: String): ApolloClient {
        return mutex.withLock {
            _clients.getOrPut(conference) {
                clientFor(conference, "none", conference != "all")
            }
        }
    }

    private fun clientFor(
        conference: String,
        uid: String?,
        writeToCacheAsynchronously: Boolean
    ): ApolloClient {
        val sqlNormalizedCacheFactory = SqlNormalizedCacheFactory(getDatabaseName(conference, uid))
        val memoryFirstThenSqlCacheFactory = MemoryCacheFactory(10 * 1024 * 1024)
            .chain(sqlNormalizedCacheFactory)

        return get<ApolloClient.Builder>()
            // .serverUrl("http://10.0.2.2:8080/graphql")
            .serverUrl("https://confetti-app.dev/graphql")
            .addHttpHeader("conference", conference)
            .normalizedCache(
                memoryFirstThenSqlCacheFactory,
                writeToCacheAsynchronously = writeToCacheAsynchronously
            )
            .autoPersistedQueries()
            .addInterceptor(tokenProviderInterceptor)
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
