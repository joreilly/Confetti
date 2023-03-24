@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsUiState
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.time.Duration.Companion.hours

class SessionsDetailsTest : ScreenshotTest() {
    @Test
    fun sessionDetailsScreen() = takeScreenshot(
        checks = {
            rule.onNodeWithText("Wednesday 09:30").assertIsDisplayed()
        }
    ) {
        SessionDetailView(
            uiState = SessionDetailsUiState.Success(
                "wearconf",
                SessionDetailsKey("fosdem", "14997"),
                sessionDetails,
                TimeZone.UTC
            ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            navigateToSpeaker = {},
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }

    companion object {
        val sessionTime = LocalDateTime(2023, 1, 4, 9, 30)
        val date = sessionTime.date

        val sessionDetails = SessionDetails(
            id = "14997",
            title = "Kotlin DevRoom Welcoming Remarks",
            type = "talk",
            startsAt = sessionTime,
            endsAt = sessionTime.toInstant(TimeZone.UTC).plus(1.hours)
                .toLocalDateTime(TimeZone.UTC),
            sessionDescription = "Welcoming participants to the Kotlin DevRoom @ FOSDEM 2023 - We're back in person!",
            language = "en-US",
            speakers = listOf(
                SessionDetails.Speaker(
                    __typename = "Speaker",
                    speakerDetails = SpeakerDetails(
                        id = "6477",
                        name = "Nicola Corti",
                        photoUrl = null,
                        company = null,
                        companyLogoUrl = null,
                        city = null,
                        bio = null,
                        socials = listOf()
                    )
                ), SessionDetails.Speaker(
                    __typename = "Speaker",
                    speakerDetails = SpeakerDetails(
                        id = "7079",
                        name = "Martin Bonnin",
                        photoUrl = null,
                        company = null,
                        companyLogoUrl = null,
                        city = null,
                        bio = null,
                        socials = listOf()
                    )
                ), SessionDetails.Speaker(
                    __typename = "Speaker",
                    speakerDetails = SpeakerDetails(
                        id = "7934",
                        name = "Holger Steinhauer",
                        photoUrl = null,
                        company = null,
                        companyLogoUrl = null,
                        city = null,
                        bio = null,
                        socials = listOf()
                    )
                )
            ),
            room = SessionDetails.Room(name = "UB5.230"),
            tags = listOf("Kotlin"),
            __typename = ""
        )
    }
}