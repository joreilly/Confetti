package dev.johnoreilly.confetti.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/** @see [DateTimeFormatter.format] */
fun DateTimeFormatter.format(date: LocalDate): String =
    format(date.toJavaLocalDate())
