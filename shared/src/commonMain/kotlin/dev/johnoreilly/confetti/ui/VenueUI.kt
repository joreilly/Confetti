package dev.johnoreilly.confetti.ui

import androidx.compose.material3.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.johnoreilly.confetti.decompose.VenueComponent
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.VenueView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenueUI(component: VenueComponent) {
    val uiState by component.uiState.subscribeAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Venue")})
        }
    ) {
        when (val state = uiState) {
            is VenueComponent.Success -> VenueView(state.data)
            is VenueComponent.Loading -> LoadingView()
            is VenueComponent.Error -> ErrorView {}
        }
    }
}

