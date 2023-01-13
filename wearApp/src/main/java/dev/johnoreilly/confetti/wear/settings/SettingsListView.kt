@file:OptIn(
    ExperimentalHorologistComposeLayoutApi::class,
    ExperimentalHorologistBaseUiApi::class
)

package dev.johnoreilly.confetti.wear.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import com.google.android.horologist.base.ui.ExperimentalHorologistBaseUiApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes

@Composable
fun SettingsListView(
    conferenceCleared: () -> Unit,
    columnState: ScalingLazyColumnState
) {
    ScalingLazyColumn(
        columnState = columnState,
    ) {
        item {
            ListHeader {
                Text(text = "Settings")
            }
        }

        item {
            StandardChip(label = "Change Conference", onClick = conferenceCleared)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun SettingsListViewPreview() {
    ConfettiTheme {
        SettingsListView(
            conferenceCleared = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
        )
    }
}
