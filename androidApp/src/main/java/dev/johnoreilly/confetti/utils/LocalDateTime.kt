package dev.johnoreilly.confetti.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

/** @see [DateTimeFormatter.format] */
fun DateTimeFormatter.format(date: LocalDate): String =
    format(date.toJavaLocalDate())

/** @see [DateTimeFormatter.format] */
fun DateTimeFormatter.format(date: LocalDateTime): String =
    format(date.toJavaLocalDateTime())
