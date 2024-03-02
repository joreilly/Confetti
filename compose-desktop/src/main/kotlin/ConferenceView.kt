import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.johnoreilly.confetti.GetConferencesQuery



private enum class NavType {
    HOME, SEARCH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConferenceView(conference: GetConferencesQuery.Conference, onBackClicked: () -> Unit) {

    var navItemState by remember { mutableStateOf(NavType.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = conference.name) },
                navigationIcon = {
                    IconButton(onClick = { onBackClicked() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ArrowBack")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    selected = navItemState == NavType.HOME,
                    onClick = { navItemState = NavType.HOME },
                    label = { Text(text = "Home") },
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                    selected = navItemState == NavType.SEARCH,
                    onClick = { navItemState = NavType.SEARCH },
                    label = { Text(text = "Search") }
                )
            }
        }
    ) {
        Column(Modifier.padding(it)) {
            if (navItemState == NavType.HOME) {
                SessionsView(conference)
            } else {
                GeminiQueryView(conference)
            }
        }
    }
}


