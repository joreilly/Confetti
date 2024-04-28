package dev.johnoreilly.confetti.calendar

import dev.johnoreilly.confetti.GetConferenceDataQuery

interface UserCalendar {
    val isEnabled: Boolean

    fun addConferenceEvent(data: GetConferenceDataQuery.Data)
}