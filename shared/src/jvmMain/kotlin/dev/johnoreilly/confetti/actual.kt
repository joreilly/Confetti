package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.utils.DateTimeFormatter
import dev.johnoreilly.confetti.utils.JvmDateTimeFormatter

actual fun getDatabaseName(conference: String) = "jdbc:sqlite:$conference.db"

actual val dateTimeFormatter: DateTimeFormatter = JvmDateTimeFormatter()