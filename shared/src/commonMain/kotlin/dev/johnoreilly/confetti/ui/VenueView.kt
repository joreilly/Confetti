package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.johnoreilly.confetti.decompose.Venue


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
        if (mapLink != null && address != null) {
            Text(text = address)
        } else {
            Text(venue.address.toString(), style = MaterialTheme.typography.titleSmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(venue.description)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = venue.imageUrl,
                contentDescription = venue.name,
                contentScale = ContentScale.FillWidth
            )

        }

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
