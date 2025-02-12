package dev.johnoreilly.confetti

import com.apollographql.apollo.ConcurrencyInfo
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.MutableExecutionOptions
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.cacheInfo
import com.apollographql.cache.normalized.fetchFromCache
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.cache.normalized.fetchPolicyInterceptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

/**
 * Use our custom fetch policy interceptor for the [FetchPolicy.CacheFirst] policy, and the Apollo built-in interceptors
 * for the other policies.
 */
fun <T> MutableExecutionOptions<T>.fetchPolicy(fetchPolicy: FetchPolicy) = when (fetchPolicy) {
    FetchPolicy.CacheFirst -> {
        fetchPolicyInterceptor(CacheFirstInterceptor)
    }

    else -> {
        fetchPolicy(fetchPolicy)
    }
}

/**
 * Returns the cached data if available (even if stale), or the network data if not.
 * If the cache data is stale, it is refreshed from the network in the background.
 */
private object CacheFirstInterceptor : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain
    ): Flow<ApolloResponse<D>> = flow {
        val cacheResponse = chain.proceed(
            request.newBuilder()
                .fetchFromCache(true)
                .build()
        ).single()
        if (cacheResponse.cacheInfo!!.isCacheHit) {
            emit(cacheResponse)
            if (cacheResponse.cacheInfo!!.isStale) {
                val scope = @OptIn(ApolloExperimental::class) request.executionContext[ConcurrencyInfo]!!.coroutineScope
                scope.launch {
                    chain.proceed(request).collect()
                }
            }
        } else {
            emitAll(chain.proceed(request))
        }
    }
}
