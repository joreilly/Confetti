package dev.johnoreilly.confetti

import kotlinx.datetime.TimeZone

fun String.toTimeZone(): TimeZone {
    return TimeZone.of(this)
}
