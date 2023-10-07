package dev.johnoreilly.confetti.venue

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.GetVenueQuery
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.decompose.VenueComponent
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.HomeScaffold
import dev.johnoreilly.confetti.ui.LoadingView


@Composable
fun VenueRoute(
    component: VenueComponent,
    windowSizeClass: WindowSizeClass,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    val uiState by component.uiState.subscribeAsState()

    HomeScaffold(
        title = stringResource(R.string.venue),
        windowSizeClass = windowSizeClass,
        topBarActions = topBarActions,
    ) {
        when (val uiState1 = uiState) {
            is VenueComponent.Success -> {
                VenueView(uiState1.data)
            }

            is VenueComponent.Loading -> LoadingView()
            is VenueComponent.Error -> ErrorView {
            }
        }
    }
}




@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun VenueView(venue: GetVenueQuery.Venue) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(venue.name, style = MaterialTheme.typography.titleLarge)
        Text(venue.address.toString(), style = MaterialTheme.typography.titleSmall)
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(venue.description)
        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = venue.imageUrl,
            contentDescription = venue.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        venue.floorPlanUrl?.let { floorPlanUrl ->
            AsyncImage(
                model = venue.floorPlanUrl,
                contentDescription = venue.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}

