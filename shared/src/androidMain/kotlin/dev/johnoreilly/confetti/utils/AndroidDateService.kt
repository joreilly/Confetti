@file:OptIn(ExperimentalTime::class)

package dev.johnoreilly.confetti.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.Instant
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinInstant

class AndroidDateService: DateService {
    override fun now(): LocalDateTime = java.time.LocalDateTime.now().toKotlinLocalDateTime()
}

// TODO these should call a method on DateService
fun LocalDateTime.Companion.nowAtTimeZone(timeZone: TimeZone) =
    Instant.now().toKotlinInstant().toLocalDateTime(timeZone)