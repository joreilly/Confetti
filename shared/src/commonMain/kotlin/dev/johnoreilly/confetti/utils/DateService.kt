package dev.johnoreilly.confetti.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.Duration

interface DateService {
    fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String

    fun now(): LocalDateTime
}

fun DateService.createCurrentLocalDateTimeFlow(delay: Duration): Flow<LocalDateTime> =
    flow {
        emit(now())
        while (currentCoroutineContext().isActive) {
            emit(now())
            delay(delay)
        }
    }
