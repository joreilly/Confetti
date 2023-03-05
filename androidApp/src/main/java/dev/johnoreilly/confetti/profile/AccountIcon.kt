package dev.johnoreilly.confetti.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun AccountIcon(
    isSignedIn: Boolean,
    avatarUrl: String?,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignout: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { showMenu = !showMenu }) {
        Icon(Icons.Default.AccountCircle, contentDescription = "menu")
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        if (isSignedIn) {
            DropdownMenuItem(
                text = { Text("Sign out") },
                onClick = {
                    showMenu = false
                    onSignout()
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