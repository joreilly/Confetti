package dev.johnoreilly.confetti.wear.complication

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.johnoreilly.confetti.wear.preview.TestFixtures

@Composable
@Preview
fun SessionPreview() {
    val context = LocalContext.current
    val renderer = NextSessionTemplate(context)

    ComplicationRendererPreview(
        complicationRenderer = renderer,
        data = NextSessionComplicationData(
            sessionDetails = TestFixtures.sessionDetails,
            conference = TestFixtures.kotlinConf2023Config,
            launchIntent = null
        )
    )
}

@Composable
@Preview
fun ConferencePreview() {
    val context = LocalContext.current
    val renderer = NextSessionTemplate(context)

    ComplicationRendererPreview(
        complicationRenderer = renderer,
        data = NextSessionComplicationData(
            conference = TestFixtures.kotlinConf2023Config,
            launchIntent = null
        )
    )
}

@Composable
@Preview
fun ConfettiPreview() {
    val context = LocalContext.current
    val renderer = NextSessionTemplate(context)

    ComplicationRendererPreview(
        complicationRenderer = renderer,
        data = NextSessionComplicationData(
            launchIntent = null
        )
    )
}