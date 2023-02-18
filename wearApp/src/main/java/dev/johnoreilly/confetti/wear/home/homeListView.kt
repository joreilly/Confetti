@file:OptIn(
    ExperimentalHorologistComposeLayoutApi::class, ExperimentalHorologistBaseUiApi::class
)

package dev.johnoreilly.confetti.wear.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.foundation.lazy.items
import com.google.android.horologist.base.ui.ExperimentalHorologistBaseUiApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.sessions.SessionView
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeListView(
    uiState: SessionsUiState,
    daySelected: (sessionId: LocalDate) -> Unit,
    onRefreshClick: () -> Unit,
    columnState: ScalingLazyColumnState
) {
    when (uiState) {
        SessionsUiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center))
        }

        is SessionsUiState.Success -> HomeList(uiState, daySelected, onRefreshClick, columnState)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeList(
    uiState: SessionsUiState.Success,
    daySelected: (sessionId: LocalDate) -> Unit,
    onRefreshClick: () -> Unit,
    columnState: ScalingLazyColumnState
) {
    // Monday
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }

    ScalingLazyColumn(
        columnState = columnState,
    ) {
        item {
            ListHeader {
                Text("Conference Days")
            }
        }

        items(uiState.confDates.size) {
            // TODO format date
            val date = uiState.confDates[it]
            StandardChip(
                label = dayFormatter.format(date.toJavaLocalDate()),
                onClick = { daySelected(date) }
            )
        }

    }
}

