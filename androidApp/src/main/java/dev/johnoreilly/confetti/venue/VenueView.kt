package dev.johnoreilly.confetti.venue

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.venue
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.decompose.Venue
import dev.johnoreilly.confetti.decompose.VenueComponent
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.HomeScaffold
import dev.johnoreilly.confetti.ui.LoadingView
import org.jetbrains.compose.resources.stringResource


@Composable
fun VenueRoute(
    component: VenueComponent,
    windowSizeClass: WindowSizeClass,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    val uiState by component.uiState.subscribeAsState()

    HomeScaffold(
        title = stringResource(Res.string.venue),
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
fun VenueView(venue: Venue) {
    val context = LocalContext.current

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
            ClickableText(
                text = AnnotatedString(
                    text = address,
                    spanStyle = SpanStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        textDecoration = TextDecoration.Underline
                    )
                ),
                style = MaterialTheme.typography.titleSmall,
                onClick = {
                    val gmmIntentUri = Uri.parse(mapLink)
                    val mapsIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    context.startActivity(mapsIntent)
                }
            )
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
        venue.floorPlanUrl?.let { floorPlanUrl ->
            VenueFloorPlanButton(venue = venue, onClick = {
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(floorPlanUrl))
                    context.startActivity(intent)
                }.getOrElse { error ->
                    error.printStackTrace()
                }
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenueFloorPlanButton(
    modifier: Modifier = Modifier,
    venue: Venue,
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            AsyncImage(
                model = venue.floorPlanUrl,
                contentDescription = venue.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )

            Icon(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                imageVector = Icons.Filled.ZoomIn,
                contentDescription = null
            )
        }
    }
}
