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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.ui.ConfettiTheme

data class AccountInfo(
    val photoUrl: String? = null,
)

@Composable
fun AccountIcon(
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onShowSettings: () -> Unit,
    installOnWear: () -> Unit,
    wearSettingsUiState: WearUiState,
    info: AccountInfo?,
) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { showMenu = !showMenu }) {
        when {
            info?.photoUrl != null -> {
                AsyncImage(
                    model = info.photoUrl,
                    contentDescription = "menu",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                )
            }

            info != null -> {
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
        if (info != null) {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.sign_out)) },
                onClick = {
                    onSignOut()
                    showMenu = false
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.sign_in_lowercase)) },
                onClick = {
                    showMenu = false
                    onSignIn()
                }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.switch_conference)) },
            onClick = {
                showMenu = false
                onSwitchConference()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.settings_title)) },
            onClick = {
                showMenu = false
                onShowSettings()
            }
        )
        if (wearSettingsUiState.showInstallOnWear) {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.install_wear)) },
                onClick = {
                    showMenu = false
                    installOnWear()
                }
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, name = "night theme")
@Preview(uiMode = UI_MODE_NIGHT_NO, name = "light theme")
@Composable
private fun AccountIconPreview() {
    val wearUiState = WearUiState()
    ConfettiTheme {
        Surface {
            AccountIcon(
                onSwitchConference = {},
                onSignIn = {},
                onSignOut = {},
                onShowSettings = {},
                installOnWear = {},
                wearSettingsUiState = wearUiState,
                info = AccountInfo(),
            )
        }
    }
}
