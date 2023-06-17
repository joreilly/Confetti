package dev.johnoreilly.confetti.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.*

class MacOSDateService: DateService {
    private val nsDateFormatter = NSDateFormatter()

    /**
     * XXX: remove timeZone and use https://github.com/Kotlin/kotlinx-datetime/discussions/253
     * when available
     */
    override fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String {
        val date = NSDate.dateWithTimeIntervalSince1970(localDateTime.toInstant(timeZone).epochSeconds.toDouble())

        return getFormatter(format, timeZone = timeZone).stringFromDate(date)
    }

    override fun now(): LocalDateTime {
        return NSDate.now().toKotlinInstant().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    private fun getFormatter(format: String, timeZone: TimeZone, locale: NSLocale = NSLocale.currentLocale()) =
        nsDateFormatter.apply {
            setDateFormat(format)
            setTimeZone(timeZone.toNSTimeZone())
            setLocale(locale)
        }
}