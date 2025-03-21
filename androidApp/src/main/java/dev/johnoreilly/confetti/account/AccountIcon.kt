//package dev.johnoreilly.confetti.account
//
//import android.content.res.Configuration.UI_MODE_NIGHT_NO
//import android.content.res.Configuration.UI_MODE_NIGHT_YES
//import androidx.compose.foundation.layout.size
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccountCircle
//import androidx.compose.material.icons.outlined.AccountCircle
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import confetti.shared.generated.resources.Res
//import confetti.shared.generated.resources.install_wear
//import confetti.shared.generated.resources.recommendations
//import confetti.shared.generated.resources.settings_title
//import confetti.shared.generated.resources.sign_in_lowercase
//import confetti.shared.generated.resources.sign_out
//import confetti.shared.generated.resources.switch_conference
//import dev.johnoreilly.confetti.R
//import dev.johnoreilly.confetti.ui.ConfettiTheme
//import org.jetbrains.compose.resources.stringResource
//
//data class AccountInfo(
//    val photoUrl: String? = null,
//)
//
//@Composable
//fun AccountIcon(
//    onSwitchConference: () -> Unit,
//    onGetRecommendations: () -> Unit,
//    onSignIn: () -> Unit,
//    onSignOut: () -> Unit,
//    onShowSettings: () -> Unit,
//    installOnWear: () -> Unit,
//    wearSettingsUiState: WearUiState,
//    info: AccountInfo?,
//    showRecommendationsOption: Boolean
//) {
//    var showMenu by remember { mutableStateOf(false) }
//
//    IconButton(onClick = { showMenu = !showMenu }) {
//        when {
//            info?.photoUrl != null -> {
//                AsyncImage(
//                    model = info.photoUrl,
//                    contentDescription = "menu",
//                    contentScale = ContentScale.Fit,
//                    modifier = Modifier
//                        .size(30.dp)
//                        .clip(androidx.compose.foundation.shape.CircleShape)
//                )
//            }
//
//            info != null -> {
//                Icon(Icons.Filled.AccountCircle, contentDescription = "menu")
//            }
//
//            else -> {
//                Icon(Icons.Outlined.AccountCircle, contentDescription = "menu")
//            }
//        }
//    }
//
//    DropdownMenu(
//        expanded = showMenu,
//        onDismissRequest = { showMenu = false }
//    ) {
//        if (info != null) {
//            DropdownMenuItem(
//                text = { Text(stringResource(Res.string.sign_out)) },
//                onClick = {
//                    onSignOut()
//                    showMenu = false
//                }
//            )
//        } else {
//            DropdownMenuItem(
//                text = { Text(stringResource(Res.string.sign_in_lowercase)) },
//                onClick = {
//                    showMenu = false
//                    onSignIn()
//                }
//            )
//        }
//        DropdownMenuItem(
//            text = { Text(stringResource(Res.string.switch_conference)) },
//            onClick = {
//                showMenu = false
//                onSwitchConference()
//            }
//        )
//
//        if (showRecommendationsOption) {
//            DropdownMenuItem(
//                text = { Text(stringResource(Res.string.recommendations)) },
//                onClick = {
//                    showMenu = false
//                    onGetRecommendations()
//                }
//            )
//        }
//
//        DropdownMenuItem(
//            text = { Text(stringResource(Res.string.settings_title)) },
//            onClick = {
//                showMenu = false
//                onShowSettings()
//            }
//        )
//        if (wearSettingsUiState.showInstallOnWear) {
//            DropdownMenuItem(
//                text = { Text(stringResource(Res.string.install_wear)) },
//                onClick = {
//                    showMenu = false
//                    installOnWear()
//                }
//            )
//        }
//    }
//}
//
//@Preview(uiMode = UI_MODE_NIGHT_YES, name = "night theme")
//@Preview(uiMode = UI_MODE_NIGHT_NO, name = "light theme")
//@Composable
//private fun AccountIconPreview() {
//    val wearUiState = WearUiState()
//    ConfettiTheme {
//        Surface {
//            AccountIcon(
//                onSwitchConference = {},
//                onGetRecommendations = {},
//                onSignIn = {},
//                onSignOut = {},
//                onShowSettings = {},
//                installOnWear = {},
//                wearSettingsUiState = wearUiState,
//                info = AccountInfo(),
//                showRecommendationsOption = false
//            )
//        }
//    }
//}
