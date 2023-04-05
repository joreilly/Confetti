package dev.johnoreilly.confetti.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface DateService {
    fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String

    fun now(): LocalDateTime
}

fun DateService.nowInstant(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant =
    now().toInstant(timeZone)

fun DateService.createCurrentLocalDateTimeFlow(delay: Duration = 5.minutes): Flow<LocalDateTime> =
    flow {
        emit(now())
        while (currentCoroutineContext().isActive) {
            emit(now())
            delay(delay)
        }
    }
