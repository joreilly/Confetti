package dev.johnoreilly.confetti.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import dev.johnoreilly.confetti.GetConferenceDataQuery
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class AndroidUserCalendar(
    var context: Context
) : UserCalendar {
    override val isEnabled: Boolean
        get() = true

    override fun addConferenceEvent(data: GetConferenceDataQuery.Data) {
        val start = data.sessions.nodes.minOf { it.sessionDetails.endsAt }
        val end = data.sessions.nodes.maxOf { it.sessionDetails.endsAt }
        val timeZone = TimeZone.of(data.config.timezone)

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.toInstant(timeZone).toEpochMilliseconds())
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.toInstant(timeZone).toEpochMilliseconds())
            .putExtra(CalendarContract.Events.TITLE, data.config.name)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, data.venues.first().name)
            .putExtra(CalendarContract.Events.EVENT_TIMEZONE, data.config.timezone)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        (AndroidUserCalendar.context ?: context).startActivity(intent)

//        val values = ContentValues().apply {
//            put(CalendarContract.Events.DTSTART, start.toInstant(timeZone).toEpochMilliseconds())
//            put(CalendarContract.Events.DTEND, end.toInstant(timeZone).toEpochMilliseconds())
//            put(CalendarContract.Events.TITLE, data.config.name)
//            put(CalendarContract.Events.DESCRIPTION, data.config.name)
//            put(CalendarContract.Events.CALENDAR_ID, data.config.id.hashCode().toLong())
//            put(CalendarContract.Events.EVENT_TIMEZONE, data.config.timezone)
//        }

//        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    }

    companion object {
        var context: Context? = null
    }
}