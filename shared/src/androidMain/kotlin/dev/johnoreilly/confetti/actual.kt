package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.utils.AndroidDateTimeFormatter
import dev.johnoreilly.confetti.utils.DateTimeFormatter

actual val dateTimeFormatter: DateTimeFormatter = AndroidDateTimeFormatter()

actual fun getDatabaseName(conference: String) = "$conference.db"