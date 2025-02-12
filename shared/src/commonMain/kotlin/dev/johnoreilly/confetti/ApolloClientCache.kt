package dev.johnoreilly.confetti

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.ExecutionContext
import com.apollographql.apollo.api.Operation
import com.apollographql.cache.normalized.apolloStore
import com.apollographql.cache.normalized.normalizedCache
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.apollo.network.NetworkMonitor
import com.apollographql.cache.normalized.api.CacheControlCacheResolver
import com.apollographql.cache.normalized.api.SchemaCoordinatesMaxAgeProvider
import com.apollographql.cache.normalized.maxStale
import com.apollographql.cache.normalized.storeReceiveDate
import dev.johnoreilly.confetti.cache.Cache
import dev.johnoreilly.confetti.di.getNormalizedCacheFactory
import dev.johnoreilly.confetti.utils.registerApolloDebugServer
import dev.johnoreilly.confetti.utils.unregisterApolloDebugServer
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Duration

interface TokenProvider {
    suspend fun token(forceRefresh: Boolean): String?
}


class TokenProviderContext(val tokenProvider: TokenProvider) : ExecutionContext.Element {
    override val key: ExecutionContext.Key<*>
        get() = Key

    companion object Key : ExecutionContext.Key<TokenProviderContext>
}

class ApolloClientCache : KoinComponent {
    private val _clients = mutableMapOf<String, ApolloClient>()
    private val mutex = reentrantLock()

    private val tokenProviderInterceptor = object : ApolloInterceptor {
        override fun <D : Operation.Data> intercept(
            request: ApolloRequest<D>,
            chain: ApolloInterceptorChain
        ): Flow<ApolloResponse<D>> = flow {
            val tokenProvider = request.executionContext[TokenProviderContext]?.tokenProvider
            if (tokenProvider == null) {
                emitAll(chain.proceed(request))
                return@flow
            }

            val token = tokenProvider.token(false)

            if (token == null) {
                emitAll(chain.proceed(request))
                return@flow
            }
            val newRequest = request.newBuilder().addHttpHeader("Authorization", "Bearer $token").build()

            val flow = chain.proceed(newRequest).onEach {
                val exception = it.exception
                if (exception is ApolloHttpException && exception.statusCode == 401) {
                    throw exception
                }
            }.catch {
                val token2 = tokenProvider.token(true)
                if (token2 != null) {
                    val newRequest2 = request.newBuilder().addHttpHeader("Authorization", "Bearer $token2").build()
                    emitAll(chain.proceed(newRequest2))
                } else {
                    emit(ApolloResponse.Builder(request.operation, request.requestUuid).exception(it as ApolloException).build())
                }
            }

            emitAll(flow)
        }


    }

    fun getClient(conference: String, uid: String?): ApolloClient {
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

    @OptIn(ApolloExperimental::class)
    private fun clientFor(
        conference: String,
        uid: String?,
        writeToCacheAsynchronously: Boolean
    ): ApolloClient {

        val normalizedCacheFactory = getNormalizedCacheFactory(conference, uid)
        return get<ApolloClient.Builder>()
//            .networkMonitor(
//                object : NetworkMonitor {
//                    override val isOnline: Boolean
//                        get() = true
//                    override suspend fun waitForNetwork() {}
//                    override fun close() {}
//                }
//            )
            .addHttpHeader("conference", conference)
            .normalizedCache(
                normalizedCacheFactory,
                writeToCacheAsynchronously = writeToCacheAsynchronously,
                cacheResolver = CacheControlCacheResolver(
                    SchemaCoordinatesMaxAgeProvider(
                        maxAges = Cache.maxAges,
                        defaultMaxAge = Duration.INFINITE,
                    )
                ),
            )
            .storeReceiveDate(true)
            .maxStale(Duration.INFINITE)
            .autoPersistedQueries()
            .addInterceptor(tokenProviderInterceptor)
            .build()
            .also {
                it.registerApolloDebugServer(conference + uid)
            }
    }

    fun close() {
        _clients.values.forEach {
            it.close()
            it.unregisterApolloDebugServer()
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
