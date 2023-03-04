package dev.johnoreilly.confetti.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

interface DateTimeFormatter {
    fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String
}