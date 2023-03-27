package dev.johnoreilly.confetti.account

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.ui.ConfettiTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun AccountIcon(
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onShowSettings: () -> Unit,
    user: User?,
    viewModel: AccountViewModel = getViewModel(),
) {
    val accountUiState = viewModel.uiState.collectAsState().value
    AccountIcon(
        onSwitchConference = onSwitchConference,
        onSignIn = onSignIn,
        onSignOut = onSignOut,
        onShowSettings = onShowSettings,
        installOnWear = viewModel::installOnWear,
        updateWearTheme = viewModel::updateWearTheme,
        uiState = accountUiState,
        user = user,
    )
}

@Composable
private fun AccountIcon(
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onShowSettings: () -> Unit,
    updateWearTheme: () -> Unit,
    installOnWear: () -> Unit,
    uiState: UiState,
    user: User?,
) {
    var showMenu by remember { mutableStateOf(false) }

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
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                showMenu = false
                onShowSettings()
            }
        )
        if (uiState.showInstallOnWear) {
            DropdownMenuItem(
                text = { Text("Install on Wear") },
                onClick = {
                    showMenu = false
                    installOnWear()
                }
            )
        }
        if (uiState.isInstalledOnWear) {
            DropdownMenuItem(
                text = { Text("Update Wear Theme") },
                onClick = {
                    showMenu = false
                    updateWearTheme()
                }
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, name = "night theme")
@Preview(uiMode = UI_MODE_NIGHT_NO, name = "light theme")
@Composable
private fun AccountIconPreview() {
    val accountUiState = UiState()
    ConfettiTheme {
        Surface {
            AccountIcon(
                onSwitchConference = {},
                onSignIn = {},
                onSignOut = {},
                onShowSettings = {},
                updateWearTheme = {},
                installOnWear = {},
                uiState = accountUiState,
                user = object: User {
                    override val name: String
                        get() = "Foo"
                    override val email: String?
                        get() = "foo@bar.com"
                    override val photoUrl: String?
                        get() = null
                    override val uid: String
                        get() = "123"

                    override suspend fun token(forceRefresh: Boolean): String? = null
                }
            )
        }
    }
}
