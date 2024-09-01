package dev.johnoreilly.confetti.backend

/**
 * Remove when https://github.com/apollographql/apollo-kotlin-execution/commit/d0c5340d0e437004b45d35cfec37d6da05532b78
 * is released
 */
sealed interface GraphQLResult<out T> {
  val isFailure: Boolean
    get() = this is GraphQLError

  val isSuccess: Boolean
    get() = this is GraphQLSuccess

  fun getOrThrow(): T = when(this) {
    is GraphQLSuccess -> value
    is GraphQLError -> throw exception
  }

  fun exceptionOrNull(): Exception? = when(this) {
    is GraphQLError -> exception
    is GraphQLSuccess -> null
  }

  fun <R> fold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R
  ): R {
    return when (this) {
      is GraphQLSuccess -> onSuccess(value)
      is GraphQLError -> onFailure(exception)
    }
  }
}

class GraphQLSuccess<out T>(val value: T): GraphQLResult<T>
class GraphQLError<out T>(val exception: Exception): GraphQLResult<T> {
  constructor(message: String): this(Exception(message))
}

fun <T, R> GraphQLResult<T>.flatMap(block: (T) -> GraphQLResult<R>): GraphQLResult<R> {
  return when (this) {
    is GraphQLError -> {
      @Suppress("UNCHECKED_CAST")
      this as GraphQLResult<R>
    }
    is GraphQLSuccess -> {
      block(value)
    }
  }
}



