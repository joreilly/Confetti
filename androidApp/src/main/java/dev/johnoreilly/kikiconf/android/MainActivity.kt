package dev.johnoreilly.kikiconf.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.johnoreilly.kikiconf.KikiConfRepository
import dev.johnoreilly.kikiconf.model.Session
import dev.johnoreilly.kikiconf.model.Speaker


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainLayout()
        }
    }
}

sealed class Screen(val title: String) {
    object SessionList : Screen("Session List")
    object SpeakerList : Screen("Speaker List")
}

data class BottomNavigationitem(val route: String, val icon: Int, val iconContentDescription: String)

val bottomNavigationItems = listOf(
    BottomNavigationitem(Screen.SessionList.title, R.drawable.ic_movie, Screen.SessionList.title),
    BottomNavigationitem(Screen.SpeakerList.title, R.drawable.ic_face, Screen.SpeakerList.title)
)


@Composable
fun MainLayout() {
    val navController = rememberNavController()
    val repo = remember { KikiConfRepository() }

    val sessionList by produceState(initialValue = emptyList<Session>(), repo) {
        value = repo.getSessions()
    }

    val speakerList by produceState(initialValue = emptyList<Speaker>(), repo) {
        value = repo.getSpeakers()
    }

    Scaffold(
        topBar = { TopAppBar (title = { Text("Sessions") } ) },
        bottomBar = { KikiConfBottomNavigation(navController) }
    ) {

        NavHost(navController, startDestination = Screen.SessionList.title) {
            composable(Screen.SessionList.title) {
                SessionList(sessionList)
            }
            composable(Screen.SpeakerList.title) {
                SpeakerList(speakerList)
            }
        }
    }
}

@Composable
fun SessionList(sessionList: List<Session>) {
    LazyColumn {
        items(sessionList) { session ->
            SessionView(session)
        }
    }
}


@Composable
fun SessionView(session: Session) {
    ListItem(
        text = { Text(session.title, style = MaterialTheme.typography.h6) }
    )
    Divider()
}

@Composable
fun SpeakerList(speakerList: List<Speaker>) {
    LazyColumn {
        items(speakerList) { speaker ->
            SpeakerView(speaker)
        }
    }
}


@Composable
fun SpeakerView(speaker: Speaker) {
    ListItem(
        text = { Text(speaker.name, style = MaterialTheme.typography.h6) },
        secondaryText = { Text(speaker.company ?: "", style = MaterialTheme.typography.subtitle1, color = Color.DarkGray) }
    )
    Divider()
}



@Composable
private fun KikiConfBottomNavigation(navController: NavHostController) {

    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavigationItems.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(item.icon), contentDescription = item.iconContentDescription) },
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
