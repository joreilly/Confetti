package dev.johnoreilly.confetti.ui.venue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import dev.johnoreilly.confetti.ui.component.FullScreenPhotoDialog
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.johnoreilly.confetti.decompose.Venue
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.sampleVenue


import dev.johnoreilly.confetti.ui.LocalBottomNavigationPadding

@Composable
fun VenueView(venue: Venue) {
    val uriHandler = LocalUriHandler.current
    var showFullScreenPhoto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 16.dp + LocalBottomNavigationPadding.current
            ),
        horizontalAlignment = Alignment.Start
    ) {
        // 1. Venue Image Banner
        if (!venue.imageUrl.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showFullScreenPhoto = true },
                shape = RoundedCornerShape(16.dp)
            ) {
                AsyncImage(
                    model = venue.imageUrl,
                    contentDescription = venue.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Venue Name
        Text(
            text = venue.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3. Venue Address with Location Icon & Map Button
        venue.address?.let { address ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!venue.mapLink.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { uriHandler.openUri(venue.mapLink) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = "Map"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View on Map")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Description
        if (venue.description.isNotBlank()) {
            Text(
                text = venue.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. Floor Plan
        venue.floorPlanUrl?.let { floorPlanUrl ->
            Text(
                text = "Floor Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            VenueFloorPlanButton(venue = venue)
        }
    }

    if (showFullScreenPhoto) {
        FullScreenPhotoDialog(
            photoUrl = venue.imageUrl,
            contentDescription = venue.name,
            onDismissRequest = { showFullScreenPhoto = false }
        )
    }
}

@Composable
fun VenueFloorPlanButton(
    modifier: Modifier = Modifier,
    venue: Venue
) {
    var showFullScreenMap by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable { showFullScreenMap = true },
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

    if (showFullScreenMap) {
        FullScreenPhotoDialog(
            photoUrl = venue.floorPlanUrl,
            contentDescription = venue.name,
            onDismissRequest = { showFullScreenMap = false }
        )
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
