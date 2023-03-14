package dev.johnoreilly.confetti.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime

class AndroidDateService: DateService {
    override fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
        return formatter.format(localDateTime.toJavaLocalDateTime())
    }

    override fun now(): LocalDateTime = java.time.LocalDateTime.now().toKotlinLocalDateTime()
}