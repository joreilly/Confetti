package dev.johnoreilly.confetti.utils

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.exception.ApolloException
import kotlinx.coroutines.flow.Flow
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
            QueryResult.Success(mapper(next.data!!))
        } else if (apolloException != null && previous is QueryResult.Loading) {
            QueryResult.Error(apolloException)
        } else {
            previous
        }
    }
}

sealed interface QueryResult<out T> {
    data class Error(
        val exception: Exception
    ) : QueryResult<Nothing>

    object Loading : QueryResult<Nothing>

    data class Success<T>(
        val result: T
    ) : QueryResult<T>
}