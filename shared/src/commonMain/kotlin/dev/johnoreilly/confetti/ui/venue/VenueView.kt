package dev.johnoreilly.confetti.ui.venue

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.johnoreilly.confetti.decompose.Venue
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.sampleVenue


@Composable
fun VenueView(venue: Venue) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(venue.name, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        val mapLink = venue.mapLink
        val address = venue.address
        Text(venue.address.toString(), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(venue.description)
        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            modifier = Modifier.height(300.dp),
            model = venue.imageUrl,
            contentDescription = venue.name,
        )

        Spacer(modifier = Modifier.height(16.dp))
        venue.floorPlanUrl?.let {
            VenueFloorPlanButton(venue = venue)
        }
    }
}

@Composable
fun VenueFloorPlanButton(
    modifier: Modifier = Modifier,
    venue: Venue
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = venue.floorPlanUrl,
                contentDescription = venue.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@MobilePreviews
@Composable
internal fun VenueViewLoadedPreview() {
    VenueView(venue = sampleVenue)
}

@Preview(name = "With floor plan", widthDp = 411, heightDp = 1200, showBackground = true)
@Composable
internal fun VenueViewWithFloorPlanPreview() {
    VenueView(
        venue = sampleVenue.copy(
            floorPlanUrl = "https://confetti-app.dev/floorplan.png",
        ),
    )
}
