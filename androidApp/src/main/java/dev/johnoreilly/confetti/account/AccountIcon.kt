package dev.johnoreilly.confetti.account

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.ApolloClientCache
import org.koin.androidx.compose.get

@Composable
fun AccountIcon(
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val authentication = get<Authentication>()
    var user by remember { mutableStateOf(authentication.currentUser()) }
    val apolloClientCache = get<ApolloClientCache>()

    IconButton(onClick = { showMenu = !showMenu }) {
        when {
            user?.photoUrl != null -> {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = user?.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(30.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                )
            }
            user != null -> {
                Icon(Icons.Filled.AccountCircle, contentDescription = "menu")
            }
            else -> {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "menu")
            }
        }
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {

        if (user != null) {
            DropdownMenuItem(
                text = { Text("Sign out") },
                onClick = {
                    onSignOut()
                    apolloClientCache.clear()
                    authentication.signOut()
                    user = null
                    showMenu = false
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Sign in") },
                onClick = {
                    showMenu = false
                    onSignIn()
                }
            )
        }
        DropdownMenuItem(
            text = { Text("Switch Conference") },
            onClick = {
                showMenu = false
                onSwitchConference()
            }
        )
    }
}