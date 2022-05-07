package dev.johnoreilly.confetti.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

interface DateTimeFormatter {
    fun format(instant: Instant, timeZone: TimeZone, format: String): String
}