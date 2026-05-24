package dev.johnoreilly.confetti.ui

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
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.agent_assistant
import confetti.shared.generated.resources.settings_title
import confetti.shared.generated.resources.sign_in_lowercase
import confetti.shared.generated.resources.sign_out
import confetti.shared.generated.resources.switch_conference
import org.jetbrains.compose.resources.stringResource

data class AccountInfo(
    val photoUrl: String? = null,
)

@Composable
fun AccountIcon(
    onSwitchConference: () -> Unit,
    onOpenAgent: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onShowSettings: () -> Unit,
    installOnWear: () -> Unit,
    //wearSettingsUiState: WearUiState,
    info: AccountInfo?,
    showAgentOption: Boolean
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
                text = { Text(stringResource(Res.string.sign_out)) },
                onClick = {
                    onSignOut()
                    showMenu = false
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.sign_in_lowercase)) },
                onClick = {
                    showMenu = false
                    onSignIn()
                }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.switch_conference)) },
            onClick = {
                showMenu = false
                onSwitchConference()
            }
        )

        if (showAgentOption) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.agent_assistant)) },
                onClick = {
                    showMenu = false
                    onOpenAgent()
                }
            )
        }

        DropdownMenuItem(
            text = { Text(stringResource(Res.string.settings_title)) },
            onClick = {
                showMenu = false
                onShowSettings()
            }
        )
//        if (wearSettingsUiState.showInstallOnWear) {
//            DropdownMenuItem(
//                text = { Text(stringResource(Res.string.install_wear)) },
//                onClick = {
//                    showMenu = false
//                    installOnWear()
//                }
//            )
//        }
    }
}

@Preview(name = "Signed out", widthDp = 64, heightDp = 64, showBackground = true)
@Composable
internal fun AccountIconSignedOutPreview() {
    AccountIcon(
        onSwitchConference = {},
        onGetRecommendations = {},
        onSignIn = {},
        onSignOut = {},
        onShowSettings = {},
        installOnWear = {},
        info = null,
        showRecommendationsOption = false,
    )
}

@Preview(name = "Signed in", widthDp = 64, heightDp = 64, showBackground = true)
@Composable
internal fun AccountIconSignedInPreview() {
    AccountIcon(
        onSwitchConference = {},
        onGetRecommendations = {},
        onSignIn = {},
        onSignOut = {},
        onShowSettings = {},
        installOnWear = {},
        info = AccountInfo(),
        showRecommendationsOption = true,
    )
}
