package dev.johnoreilly.confetti.account

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.koin.androidx.compose.getViewModel

@Composable
fun AccountIcon(
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: AccountViewModel = getViewModel()
) {
    var showMenu by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    IconButton(onClick = { showMenu = !showMenu }) {
        when {
            user?.photoUrl != null -> {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = user.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(30.dp)
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
                    viewModel.signOut()
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
        if (uiState.showInstallOnWear) {
            DropdownMenuItem(
                text = { Text("Install on Wear") },
                onClick = {
                    showMenu = false
                    viewModel.installOnWear()
                }
            )
        }
        if (uiState.isInstalledOnWear) {
            DropdownMenuItem(
                text = { Text("Update Wear Theme") },
                onClick = {
                    showMenu = false
                    viewModel.updateWearTheme()
                }
            )
        }
    }
}