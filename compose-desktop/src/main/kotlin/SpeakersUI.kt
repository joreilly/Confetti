import androidx.compose.material.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.johnoreilly.confetti.decompose.SpeakerDetailsComponent
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState
import dev.johnoreilly.confetti.decompose.SpeakersComponent
import dev.johnoreilly.confetti.decompose.SpeakersUiState
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.SpeakerDetailsView
import dev.johnoreilly.confetti.ui.SpeakerGridView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakersUI(component: SpeakersComponent) {
    val uiState by component.uiState.subscribeAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Speakers")})
        }
    ) {
        when (val state = uiState) {
            is SpeakersUiState.Success -> {
                SpeakerGridView(state.speakers, component::onSpeakerClicked)
            }
            is SpeakersUiState.Loading -> LoadingView()
            is SpeakersUiState.Error -> ErrorView {}
        }
    }
}

@Composable
fun SpeakerDetailsUI(component: SpeakerDetailsComponent) {
    val uiState by component.uiState.subscribeAsState()

    when (val state = uiState) {
        is SpeakerDetailsUiState.Loading -> LoadingView()
        is SpeakerDetailsUiState.Error -> ErrorView()
        is SpeakerDetailsUiState.Success -> SpeakerDetailsView(
            state.details,
            component::onSessionClicked,
            component::onCloseClicked,
        )
    }

}
