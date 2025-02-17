package dev.johnoreilly.confetti

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.MutableExecutionOptions
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.cacheInfo
import com.apollographql.cache.normalized.fetchFromCache
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.cache.normalized.fetchPolicyInterceptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single

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
 * Returns by priority:
 * - the cached data if it's fresh
 * - the network data if it could be fetched
 * - the stale cached data
 */
private object CacheFirstInterceptor : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain
    ): Flow<ApolloResponse<D>> = flow {
        val cacheResponse = chain.proceed(request.newBuilder().fetchFromCache(true).build()).single()
        if (cacheResponse.cacheInfo!!.isCacheHit && !cacheResponse.cacheInfo!!.isStale) {
            emit(cacheResponse)
        } else {
            // If the first emission is an exception, emit the cache response.
            // For exceptions on subsequent emissions, emit the network responses.
            var first = true
            chain.proceed(request).collect { networkResponse ->
                emit(
                    if (networkResponse.exception == null || !first) {
                        networkResponse
                    } else {
                        cacheResponse.cacheMissAsException()
                    }
                )
                first = false
            }
        }
    }
}

private fun <D : Operation.Data> ApolloResponse<D>.cacheMissAsException(): ApolloResponse<D> {
    return if (cacheInfo!!.isCacheHit) {
        this
    } else {
        val cacheMissException =
            errors.orEmpty().mapNotNull { it.extensions?.get("exception") as? ApolloException }.reduceOrNull { acc, e ->
                acc.addSuppressed(e)
                acc
            }
        newBuilder()
            .exception(cacheMissException)
            .data(null)
            .errors(null)
            .build()
    }
}

