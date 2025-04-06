package dev.johnoreilly.confetti.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.now

class IosDateService: DateService {
    private val nsDateFormatter = NSDateFormatter()
    override fun now(): LocalDateTime {
        return NSDate.now().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault())
    }
}