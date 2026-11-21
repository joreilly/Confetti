package dev.johnoreilly.confetti

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.MutableExecutionOptions
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchFromCache
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.cache.normalized.fetchPolicyInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch

private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/**
 * Configure execution options to use [CacheFirstInterceptor] for [FetchPolicy.CacheFirst]
 * and default Apollo interceptors for other policies.
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
 * Interceptor for [FetchPolicy.CacheFirst] operations.
 *
 * Emits cached data immediately to show the UI without delay.
 * Updates the cache in the background. Network errors during background
 * sync are ignored to keep valid cached data visible.
 */
private object CacheFirstInterceptor : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain
    ): Flow<ApolloResponse<D>> = flow {
        val cacheResponse = chain.proceed(request.newBuilder().fetchFromCache(true).build()).singleOrNull()

        if (cacheResponse?.data != null) {
            // Emit cached data immediately to prevent startup loading delay.
            emit(cacheResponse)

            // Refresh cache in background.
            backgroundScope.launch { chain.proceed(request).collect() }
        } else {
            // Cache miss requires initial network fetch
            chain.proceed(request).collect { networkResponse ->
                emit(networkResponse)
            }
        }
    }
}
