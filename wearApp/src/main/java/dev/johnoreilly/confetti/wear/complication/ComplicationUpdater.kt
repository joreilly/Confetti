package dev.johnoreilly.confetti.wear.complication

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ComplicationUpdater : KoinComponent {
    fun update() {
        val context: Context = get()

        val request = ComplicationDataSourceUpdateRequester.create(
            context, ComponentName(
                context, NextSessionComplicationData::class.java
            )
        )
        request.requestUpdateAll()
    }
}