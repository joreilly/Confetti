package dev.johnoreilly.confetti.work

import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.datetime.until
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface NotificationSender {
    sealed interface Selector {
        fun matches(now: LocalDateTime, session: SessionDetails): Boolean
    }
    data class Today(val within: Duration = 15.minutes): Selector {
        override fun matches(
            now: LocalDateTime,
            session: SessionDetails
        ): Boolean {
            if (session.startsAt.date != now.date)
                return false

            val compareInAnyTz = UtcOffset(0)
            val until = session.startsAt.toInstant(compareInAnyTz).until(now.toInstant(compareInAnyTz), DateTimeUnit.MINUTE)
            return until in 0..within.inWholeMinutes
        }
    }

    data object AllFuture: Selector {
        override fun matches(
            now: LocalDateTime,
            session: SessionDetails
        ): Boolean {
            return session.startsAt > now
        }
    }

    suspend fun sendNotification(selector: Selector = Today())
}