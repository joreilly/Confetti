package dev.johnoreilly.confetti.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.*

class IosDateTimeFormatter: DateTimeFormatter {
    private val nsDateFormatter = NSDateFormatter()

    override fun format(instant: Instant, timeZone: TimeZone, format: String): String {
        val date = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())

        return getFormatter(format, timeZone = timeZone).stringFromDate(date)
    }

    private fun getFormatter(format: String, timeZone: TimeZone, locale: NSLocale = NSLocale.currentLocale()) =
        nsDateFormatter.apply {
            setDateFormat(format)
            setTimeZone(timeZone.toNSTimeZone())
            setLocale(locale)
        }
}