package dev.johnoreilly.confetti.wear.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

class ComplicationUpdater(val application: Context) {
    fun update() {
        val request = ComplicationDataSourceUpdateRequester.create(
            application, ComponentName(
                application, NextSessionComplicationData::class.java
            )
        )
        request.requestUpdateAll()
    }
}