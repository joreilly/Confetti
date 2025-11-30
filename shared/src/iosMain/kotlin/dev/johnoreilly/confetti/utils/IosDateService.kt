@file:OptIn(ExperimentalTime::class)

package dev.johnoreilly.confetti.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.now
import kotlin.time.ExperimentalTime

class IosDateService: DateService {
    override fun now(): LocalDateTime {
        return NSDate.now().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault())
    }
}