package dev.johnoreilly.confetti.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.ZoneId

class JvmDateService: DateService {
    @Suppress("NewApi")
    override fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
        return formatter.format(localDateTime.toJavaLocalDateTime())
    }

    override fun now(): LocalDateTime = java.time.LocalDateTime.now().toKotlinLocalDateTime()
}