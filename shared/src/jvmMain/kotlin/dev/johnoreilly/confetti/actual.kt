package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.utils.DateTimeFormatter
import dev.johnoreilly.confetti.utils.JvmDateTimeFormatter

actual val dateTimeFormatter: DateTimeFormatter = JvmDateTimeFormatter()

actual fun getDatabaseName(conference: String) = "jdbc:sqlite:$conference.db"