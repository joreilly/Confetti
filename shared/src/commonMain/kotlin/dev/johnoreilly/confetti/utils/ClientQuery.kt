package dev.johnoreilly.confetti.utils

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.cache.normalized.CacheInfo
import com.apollographql.apollo.cache.normalized.cacheInfo
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.CacheMissException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.runningFold

object ClientQuery {
    fun <D : Query.Data, T> ApolloCall<D>.toUiState(
        onError: (List<Error>, ApolloException?) -> Unit = { _, _ -> },
        initial: QueryResult<T> = QueryResult.Loading,
        mapper: (D) -> T
    ): Flow<QueryResult<T>> = toFlow().runningFold(initial) { previous, next ->
        val apolloException = next.exception

        if (next.hasErrors() || apolloException != null) {
            onError(next.errors.orEmpty(), apolloException)
        }

        if (next.data != null) {
            QueryResult.Success(mapper(next.data!!), next.cacheInfo)
        } else if (apolloException is CacheMissException) {
            previous
        } else if (apolloException != null && previous is QueryResult.Loading) {
            QueryResult.Error(apolloException)
        } else {
            previous
        }
    }.filterNot {
        // Loading will be added by a QueryResult.Loading from stateIn in viewModel
        // avoid here as it will cause stale results to be replaced by loading on screen
        it is QueryResult.Loading
    }
}

sealed interface QueryResult<out T> {
    data class Error(
        val exception: Exception
    ) : QueryResult<Nothing>

    object Loading : QueryResult<Nothing>

    object None : QueryResult<Nothing>

    data class Success<T>(
        val result: T,
        val cacheInfo: CacheInfo? = null
    ) : QueryResult<T>
}