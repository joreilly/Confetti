package dev.johnoreilly.confetti.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.Instant

class AndroidDateService: DateService {
    override fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
        return formatter.format(localDateTime.toJavaLocalDateTime())
    }

    override fun now(): LocalDateTime = java.time.LocalDateTime.now().toKotlinLocalDateTime()
}

fun LocalDateTime.Companion.nowAtTimeZone(timeZone: TimeZone) =
    Instant.now().toKotlinInstant().toLocalDateTime(timeZone)