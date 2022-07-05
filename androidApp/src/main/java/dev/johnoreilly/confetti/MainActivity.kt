package dev.johnoreilly.confetti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.johnoreilly.confetti.rooms.RoomListView
import dev.johnoreilly.confetti.sessions.SessionDetailView
import dev.johnoreilly.confetti.sessions.SessionListView
import dev.johnoreilly.confetti.speakers.SpeakerListView
import dev.johnoreilly.confetti.theme.ConfettiTheme
import org.koin.androidx.compose.getViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConfettiTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainLayout()
                }
            }
        }
    }
}

sealed class Screen(val title: String) {
    object SessionList : Screen("Session List")
    object SessionDetails : Screen("Session Details")
    object SpeakerList : Screen("Speaker List")
    object RoomList : Screen("Room List")
}

data class BottomNavigationitem(val route: String, val icon: ImageVector, val iconContentDescription: String)

val bottomNavigationItems = listOf(
    BottomNavigationitem(Screen.SessionList.title, Icons.Filled.PlayArrow, Screen.SessionList.title),
    BottomNavigationitem(Screen.SpeakerList.title, Icons.Default.Person, Screen.SpeakerList.title),
    BottomNavigationitem(Screen.RoomList.title, Icons.Default.LocationOn, Screen.RoomList.title)
)


@Composable
fun MainLayout() {
    val viewModel = getViewModel<ConfettiViewModel>()
    val navController = rememberNavController()


    val bottomBar: @Composable () -> Unit = { ConfettiBottomNavigation(navController) }

    NavHost(navController, startDestination = Screen.SessionList.title) {
        composable(Screen.SessionList.title) {
            SessionListView(viewModel, bottomBar) { session ->
                navController.navigate(Screen.SessionDetails.title + "/${session.id}")
            }
        }
        composable(route = Screen.SessionDetails.title + "/{id}") {
            SessionDetailView(viewModel,
                it.arguments?.get("id") as String,
                popBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SpeakerList.title) {
            SpeakerListView(viewModel, bottomBar)
        }
        composable(Screen.RoomList.title) {
            RoomListView(viewModel, bottomBar)
        }
    }
}


@Composable
private fun ConfettiBottomNavigation(navController: NavHostController) {

    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavigationItems.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.iconContentDescription) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.id)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
