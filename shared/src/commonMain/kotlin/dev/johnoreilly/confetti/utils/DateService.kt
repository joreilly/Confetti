package dev.johnoreilly.confetti.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

interface DateService {
    fun format(instant: Instant, timeZone: TimeZone, format: String): String

    fun now(): LocalDateTime
}