package dev.johnoreilly.kikiconf.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import dev.johnoreilly.kikiconf.KikiConfRepository
import dev.johnoreilly.kikiconf.model.Room
import dev.johnoreilly.kikiconf.model.Session
import dev.johnoreilly.kikiconf.model.Speaker
import dev.johnoreilly.kikiconf.ui.theme.KikiConfTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KikiConfTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainLayout()
                }
            }
        }
    }
}

sealed class Screen(val title: String) {
    object SessionList : Screen("Session List")
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
    val navController = rememberNavController()
    val repo = remember { KikiConfRepository() }

    val sessionList by produceState(initialValue = emptyList<Session>(), repo) {
        value = repo.getSessions()
    }

    val speakerList by produceState(initialValue = emptyList<Speaker>(), repo) {
        value = repo.getSpeakers()
    }

    val roomList by produceState(initialValue = emptyList<Room>(), repo) {
        value = repo.getRooms()
    }

    Scaffold(
        topBar = { TopAppBar (title = { Text("KikiConf") } ) },
        bottomBar = { KikiConfBottomNavigation(navController) }
    ) {

        NavHost(navController, startDestination = Screen.SessionList.title) {
            composable(Screen.SessionList.title) {
                SessionList(sessionList)
            }
            composable(Screen.SpeakerList.title) {
                SpeakerList(speakerList)
            }
            composable(Screen.RoomList.title) {
                RoomList(roomList)
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


@OptIn(ExperimentalMaterialApi::class)
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

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = {  })
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val personImageUrl = speaker.photoUrl ?: ""
        if (personImageUrl.isNotEmpty()) {
            Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
            ) {
                Image(painter = rememberImagePainter(speaker.photoUrl),
                        modifier = Modifier.size(60.dp),
                        contentDescription = speaker.name
                )
            }
        } else {
            Spacer(modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(text = speaker.name, style = TextStyle(fontSize = 20.sp))
            Text(text = speaker.company ?: "", style = TextStyle(color = Color.DarkGray, fontSize = 14.sp))
        }
    }

    Divider()
}


@Composable
fun RoomList(roomList: List<Room>) {
    LazyColumn {
        items(roomList) { room ->
            RoomView(room)
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RoomView(room: Room) {
    ListItem(
        text = { Text(room.name, style = MaterialTheme.typography.h6) }
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
