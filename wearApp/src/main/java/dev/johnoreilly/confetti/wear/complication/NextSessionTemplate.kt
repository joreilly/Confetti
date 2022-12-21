@file:OptIn(ExperimentalHorologistTilesApi::class)

package dev.johnoreilly.confetti.wear.complication

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import com.google.android.horologist.tiles.ExperimentalHorologistTilesApi
import com.google.android.horologist.tiles.complication.DataTemplates.longText
import com.google.android.horologist.tiles.complication.DataTemplates.shortText
import com.google.android.horologist.tiles.complication.TypedComplicationTemplate
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.datetime.toKotlinInstant
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalHorologistTilesApi::class)
class NextSessionTemplate(context: Context) :
    TypedComplicationTemplate<NextSessionComplicationData>(context) {
    override fun previewData(): NextSessionComplicationData {
        val sessionTime = LocalDateTime.of(2022, 12, 25, 12, 30)
        val startInstant = sessionTime.toInstant(ZoneOffset.UTC).toKotlinInstant()
        return NextSessionComplicationData(
            SessionDetails(
                "1",
                "Wear it's at",
                "Talk",
                startInstant,
                startInstant,
                "Be aWear of what's coming",
                "en",
                listOf(),
                SessionDetails.Room("Main Hall"),
                listOf()
            ),
            launchIntent = null
        )
    }

    override fun supportedTypes(): List<ComplicationType> =
        listOf(
            ComplicationType.SHORT_TEXT,
            ComplicationType.LONG_TEXT,
        )

    override fun renderShortText(data: NextSessionComplicationData): ShortTextComplicationData =
        shortText(
            title = data.sessionDetails?.title ?: "None",
            text = data.sessionDetails?.room?.name.orEmpty(),
            icon = R.drawable.ic_person_black_24dp,
            launchIntent = data.launchIntent
        )

    override fun renderLongText(data: NextSessionComplicationData): LongTextComplicationData? {
        return longText(
            title = data.sessionDetails?.title ?: "None",
            text = data.sessionDetails?.room?.name.orEmpty(),
            launchIntent = data.launchIntent,
            icon = null
        )
    }
}