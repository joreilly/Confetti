import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.johnoreilly.confetti.decompose.ConferenceComponent
import dev.johnoreilly.confetti.decompose.HomeComponent
import dev.johnoreilly.confetti.ui.ConferenceMaterialTheme
import dev.johnoreilly.confetti.ui.SessionListGridView


@Composable
fun ConferenceView(component: ConferenceComponent) {
    ConferenceMaterialTheme(component.conferenceThemeColor) {
        Children(
            stack = component.stack,
        ) {
            when (val child = it.instance) {
                is ConferenceComponent.Child.Home -> HomeView(child.component)
                is ConferenceComponent.Child.SessionDetails -> SessionDetailsUI(child.component)
                is ConferenceComponent.Child.SpeakerDetails -> {} //SpeakerDetailsRoute(child.component)
                is ConferenceComponent.Child.Settings -> {} //SettingsRoute(child.component)
            }
        }
    }
}


@Composable
fun HomeView(component: HomeComponent) {
    val stack by component.stack.subscribeAsState()
    val activeChild = stack.active.instance

    Scaffold(
        bottomBar = {
            BottomNavigation(backgroundColor = MaterialTheme.colorScheme.surface) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    selected = activeChild is HomeComponent.Child.Sessions,
                    onClick = component::onSessionsTabClicked,
                    label = { Text(text = "Schedule") },
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Home") },
                    selected = activeChild is HomeComponent.Child.Speakers,
                    onClick = component::onSpeakersTabClicked,
                    label = { Text(text = "Speakers") },
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Outlined.LocationOn, contentDescription = "Home") },
                    selected = activeChild is HomeComponent.Child.Venue,
                    onClick = component::onVenueTabClicked,
                    label = { Text(text = "Venue") },
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                    selected = activeChild is HomeComponent.Child.Recommendations,
                    onClick = component::onGetRecommendationsClicked,
                    label = { Text(text = "Gemini Query") }
                )
            }
        }
    ) {
        Children(
            modifier = Modifier.padding(it),
            stack = component.stack,
        ) {
            when (val child = it.instance) {
                is HomeComponent.Child.Sessions -> {
                    val uiState by child.component.uiState.subscribeAsState()
                    SessionListGridView(
                        uiState = uiState,
                        sessionSelected = child.component::onSessionClicked,
                        onRefresh = {},
                        addBookmark = {},
                        removeBookmark = {},
                        onNavigateToSignIn = {},
                        isLoggedIn = child.component.isLoggedIn,
                    )
                }
                is HomeComponent.Child.MultiPane -> Text(text = "Multi-pane mode is not yet supported")
                is HomeComponent.Child.Speakers -> {

                }
                is HomeComponent.Child.Bookmarks -> {}
                is HomeComponent.Child.Venue -> {}
                is HomeComponent.Child.Search -> {}
                is HomeComponent.Child.Recommendations -> GeminiQueryView(child.component)
            }
        }
    }
}
