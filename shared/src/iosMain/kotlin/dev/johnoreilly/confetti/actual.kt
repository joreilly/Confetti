package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.utils.DateTimeFormatter
import dev.johnoreilly.confetti.utils.IosDateTimeFormatter

actual val dateTimeFormatter: DateTimeFormatter = IosDateTimeFormatter()

actual fun getDatabaseName(conference: String) = "$conference.db"