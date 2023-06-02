package dev.johnoreilly.confetti.auto.speakers.details

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.LongMessageTemplate
import androidx.car.app.model.Template
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SpeakerDetails

class SpeakerBiographyScreen (
    carContext: CarContext,
    private val speaker: SpeakerDetails
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        return LongMessageTemplate.Builder(
            if(speaker.bio.isNullOrEmpty()) {
                carContext.getString(R.string.auto_no_information)
            } else {
                speaker.bio.toString()
            }
        )
            .setTitle(
                carContext.getString(R.string.auto_speakers_biography_more, speaker.name)
            )
            .setHeaderAction(Action.BACK)
            .build()
    }
}