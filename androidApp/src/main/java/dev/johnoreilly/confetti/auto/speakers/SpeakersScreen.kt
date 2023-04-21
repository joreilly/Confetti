package dev.johnoreilly.confetti.auto.speakers

import android.graphics.Bitmap
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.ItemList
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.SpeakersUiState
import dev.johnoreilly.confetti.SpeakersViewModel
import dev.johnoreilly.confetti.auto.speakers.details.SpeakerDetailsScreen
import dev.johnoreilly.confetti.auto.ui.ErrorScreen
import dev.johnoreilly.confetti.auto.utils.AutoImage
import dev.johnoreilly.confetti.auto.utils.AutoImageData
import dev.johnoreilly.confetti.auto.utils.fetchImage
import dev.johnoreilly.confetti.auto.utils.getDefaultBitmap
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.launch

class SpeakersScreen(
    carContext: CarContext,
    conference: String,
    private val speakersViewModel: SpeakersViewModel = SpeakersViewModel()
) : Screen(carContext) {

    private var images: MutableList<AutoImage> = mutableListOf()
    private var uiState: SpeakersUiState = SpeakersUiState.Loading

    init {
        speakersViewModel.configure(conference)
    }

    override fun onGetTemplate(): Template {
        lifecycleScope.launch {
            speakersViewModel.speakers.collect {
                uiState = it
                invalidate()
            }
        }

        var listBuilder = ItemList.Builder()
        val loading = when(val result = uiState) {
            SpeakersUiState.Loading -> {
                true
            }
            SpeakersUiState.Error -> {
                return ErrorScreen(carContext, R.string.auto_speakers_failed).onGetTemplate()
            }
            is SpeakersUiState.Success -> {
                listBuilder = createSpeakersList(result.speakers)
                false
            }
        }

        return GridTemplate.Builder().apply {
            setTitle(carContext.getString(R.string.speakers))
            setHeaderAction(Action.BACK)
            setLoading(loading)
            if (!loading) {
                setSingleList(listBuilder.build())
            }
        }.build()
    }

    private fun createSpeakersList(speakers: List<SpeakerDetails>): ItemList.Builder {
        val listBuilder = ItemList.Builder()
        for (speaker in speakers) {
            val image = images.firstOrNull { it.id == speaker.id }

            listBuilder.addItem(
                GridItem.Builder()
                    .setTitle(speaker.name)
                    .setText(speaker.company ?: "")
                    .setOnClickListener {
                        screenManager.push(SpeakerDetailsScreen(carContext, speaker))
                    }.setImage(
                        if (image == null) {
                            images.add(AutoImage(speaker.id, getDefaultBitmap(carContext)))
                            fetchImage(carContext, speaker.name, speaker.photoUrl ?: "", object : AutoImageData {

                                override fun onImageFetch(bitmap: Bitmap) {
                                    val index = images.indexOfFirst { it.id == speaker.id }
                                    if (index < 0) {
                                        images.add(AutoImage(speaker.id, bitmap))
                                    } else {
                                        images[index] = AutoImage(speaker.id, bitmap)
                                    }
                                    invalidate()
                                }
                            })

                            CarIcon.Builder(
                                IconCompat.createWithBitmap(getDefaultBitmap(carContext))
                            )
                        } else {
                            CarIcon.Builder(
                                IconCompat.createWithBitmap(image.bitmap)
                            )
                        }.build()
                    ).build()
            )
        }

        return listBuilder
    }
}