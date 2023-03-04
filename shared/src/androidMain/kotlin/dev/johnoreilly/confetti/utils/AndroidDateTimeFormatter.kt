package dev.johnoreilly.confetti.utils

import android.annotation.SuppressLint
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import java.time.ZoneId

class AndroidDateTimeFormatter: DateTimeFormatter {
    override fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
        return formatter.withZone(ZoneId.of(timeZone.id)).format(localDateTime.toJavaLocalDateTime())
    }
}