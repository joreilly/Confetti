package dev.johnoreilly.confetti.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.*

class IosDateTimeFormatter: DateTimeFormatter {
    private val nsDateFormatter = NSDateFormatter()

    override fun format(localDateTime: LocalDateTime, timeZone: TimeZone, format: String): String {
        val date = NSDate.dateWithTimeIntervalSince1970(localDateTime.toInstant(timeZone).epochSeconds.toDouble())

        return getFormatter(format, timeZone = timeZone).stringFromDate(date)
    }

    private fun getFormatter(format: String, timeZone: TimeZone, locale: NSLocale = NSLocale.currentLocale()) =
        nsDateFormatter.apply {
            setDateFormat(format)
            setTimeZone(timeZone.toNSTimeZone())
            setLocale(locale)
        }
}