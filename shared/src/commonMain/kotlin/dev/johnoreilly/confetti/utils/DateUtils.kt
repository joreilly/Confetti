import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char

fun LocalDateTime.conferenceDateFormat() = format(LocalDateTime.Format {
    dayOfMonth()
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    chars(", ")
    year()
})


@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDateTime.sessionStartDateTimeFormat() = format(LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    dayOfMonth()
    char(' ')
    byUnicodePattern("HH:mm")
})


@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDateTime.sessionTimeFormat() = format(LocalDateTime.Format {
    byUnicodePattern("HH:mm")
})