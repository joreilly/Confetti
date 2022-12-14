package dev.johnoreilly.confetti.utils

import android.annotation.SuppressLint
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId

class AndroidDateTimeFormatter: DateTimeFormatter {
    // Suppress the error below.
    // Error: Cast from Instant to TemporalAccessor requires API level 26 (current min is 21) [NewApi]
    @SuppressLint("NewApi")
    override fun format(instant: Instant, timeZone: TimeZone, format: String): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
        return formatter.withZone(ZoneId.of(timeZone.id)).format(instant.toJavaInstant())
    }
}