package dev.johnoreilly.confetti.auto.speakers.details

import android.graphics.Bitmap
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auto.utils.fetchImage
import dev.johnoreilly.confetti.auto.utils.getDefaultBitmap
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.launch

class SpeakerDetailsScreen(
    carContext: CarContext,
    private val speaker: SpeakerDetails
) : Screen(carContext) {

    private lateinit var speakerImage: Bitmap

    override fun onGetTemplate(): Template {

        val paneBuilder = Pane.Builder()
        paneBuilder.addRow(
            Row.Builder().apply {
                setTitle(carContext.getString(R.string.auto_speakers_sessions))
                for (session in speaker.sessions) {
                    addText(session.title)
                }
            }.build()
        ).setImage(
            if (::speakerImage.isInitialized) {
                CarIcon.Builder(
                    IconCompat.createWithBitmap(speakerImage)
                )
            } else {
                lifecycleScope.launch {
                    speakerImage = fetchImage(carContext, speaker.id, speaker.photoUrl ?: " ")
                    invalidate()
                }

                CarIcon.Builder(
                    IconCompat.createWithBitmap(getDefaultBitmap(carContext))
                )
            }.build()
        )

        paneBuilder.addAction(createAction().build())

        return PaneTemplate.Builder(paneBuilder.build())
            .setHeaderAction(Action.BACK)
            .setTitle(speaker.name)
            .build()
    }

    private fun createAction(): Action.Builder {
        return Action.Builder()
            .setTitle(carContext.getString(R.string.auto_speakers_biography))
            .setOnClickListener {
                screenManager.push(SpeakerBiographyScreen(carContext, speaker))
            }
    }
}