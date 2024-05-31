@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.brand_android
import confetti.shared.generated.resources.brand_default
import confetti.shared.generated.resources.dark_mode_config_dark
import confetti.shared.generated.resources.dark_mode_config_light
import confetti.shared.generated.resources.dark_mode_config_system_default
import confetti.shared.generated.resources.dark_mode_preference
import confetti.shared.generated.resources.developerSettings
import confetti.shared.generated.resources.dynamic_color_preference
import confetti.shared.generated.resources.privacy_policy
import confetti.shared.generated.resources.settings_boolean_false
import confetti.shared.generated.resources.settings_boolean_true
import confetti.shared.generated.resources.settings_title
import confetti.shared.generated.resources.theme
import confetti.shared.generated.resources.use_experimental_features
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.DarkThemeConfig
import dev.johnoreilly.confetti.DeveloperSettings
import dev.johnoreilly.confetti.ThemeBrand
import dev.johnoreilly.confetti.UserEditableSettings
import dev.johnoreilly.confetti.decompose.SettingsComponent
import dev.johnoreilly.confetti.ui.supportsDynamicTheming
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsRoute(
    component: SettingsComponent,
) {
    val userEditableSettings by component.userEditableSettings.collectAsStateWithLifecycle()
    val developerSettings by component.developerSettings.collectAsStateWithLifecycle()
    SettingsScreen(
        userEditableSettings = userEditableSettings,
        onChangeThemeBrand = component::updateThemeBrand,
        onChangeDynamicColorPreference = component::updateDynamicColorPreference,
        onChangeDarkThemeConfig = component::updateDarkThemeConfig,
        onChangeUseExperimentalFeatures = component::updateUseExperimentalFeatures,
        developerSettings = developerSettings,
        onEnableDeveloperMode = component::enableDeveloperMode
    )
}

@Composable
fun SettingsScreen(
    userEditableSettings: UserEditableSettings?,
    supportDynamicColor: Boolean = supportsDynamicTheming(),
    onChangeUseExperimentalFeatures: (value: Boolean) -> Unit,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    developerSettings: DeveloperSettings?,
    onEnableDeveloperMode: () -> Unit
) {
    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    /**
     * usePlatformDefaultWidth = false is use as a temporary fix to allow
     * height recalculation during recomposition. This, however, causes
     * Dialog's to occupy full width in Compact mode. Therefore max width
     * is configured below. This should be removed when there's fix to
     * https://issuetracker.google.com/issues/221643630
     */
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        Column(
            Modifier
                .clipToBounds()
                .padding(it)
        ) {
            HorizontalDivider(Modifier.padding(top = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        SettingsPanel(
                            settings = userEditableSettings,
                            supportDynamicColor = supportDynamicColor,
                            onChangeThemeBrand = onChangeThemeBrand,
                            onChangeDynamicColorPreference = onChangeDynamicColorPreference,
                            onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                            onChangeUseExperimentalFeatures = onChangeUseExperimentalFeatures,
                        )
                    }
                }

                item {
                    val context = LocalContext.current
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                runCatching {
                                    val browseIntent =
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://confetti-app.dev/privacy.html"))
                                    startActivity(context, browseIntent, null)
                                }.getOrElse { error ->
                                    error.printStackTrace()
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Text(stringResource(Res.string.privacy_policy))
                    }
                }

                if (developerSettings != null) {
                    item {
                        Column(modifier = Modifier.padding(8.dp)) {
                            SettingsDialogSectionTitle(text = stringResource(Res.string.developerSettings))
                            Text(
                                "Token: ${developerSettings.token}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            var developerModeCount by remember { mutableIntStateOf(0) }
            Box(modifier = Modifier.run {
                if (developerSettings == null) {
                    clickable {
                        developerModeCount++
                        if (developerModeCount > 8) {
                            onEnableDeveloperMode()
                        }
                    }
                } else {
                    this
                }
            }) {
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp),
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Version: ${BuildConfig.VERSION_NAME}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    settings: UserEditableSettings?,
    supportDynamicColor: Boolean,
    onChangeUseExperimentalFeatures: (value: Boolean) -> Unit,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    SettingsDialogSectionTitle(text = stringResource(Res.string.theme))
    if (settings != null) {
        Column(Modifier.selectableGroup()) {
            SettingsDialogThemeChooserRow(
                text = stringResource(Res.string.brand_default),
                selected = settings.brand == ThemeBrand.DEFAULT,
                onClick = { onChangeThemeBrand(ThemeBrand.DEFAULT) },
            )
            SettingsDialogThemeChooserRow(
                text = stringResource(Res.string.brand_android),
                selected = settings.brand == ThemeBrand.ANDROID,
                onClick = { onChangeThemeBrand(ThemeBrand.ANDROID) },
            )
        }
        if (settings.brand == ThemeBrand.DEFAULT && supportDynamicColor) {
            BooleanSettings(
                title = stringResource(Res.string.dynamic_color_preference),
                value = settings.useDynamicColor,
                onValueChange = { value -> onChangeDynamicColorPreference(value) }
            )
        }

        BooleanSettings(
            title = stringResource(Res.string.use_experimental_features),
            value = settings.useExperimentalFeatures,
            onValueChange = { value -> onChangeUseExperimentalFeatures(value) }
        )

        SettingsDialogSectionTitle(text = stringResource(Res.string.dark_mode_preference))
        Column(Modifier.selectableGroup()) {
            SettingsDialogThemeChooserRow(
                text = stringResource(Res.string.dark_mode_config_system_default),
                selected = settings.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
                onClick = { onChangeDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) },
            )
            SettingsDialogThemeChooserRow(
                text = stringResource(Res.string.dark_mode_config_light),
                selected = settings.darkThemeConfig == DarkThemeConfig.LIGHT,
                onClick = { onChangeDarkThemeConfig(DarkThemeConfig.LIGHT) },
            )
            SettingsDialogThemeChooserRow(
                text = stringResource(Res.string.dark_mode_config_dark),
                selected = settings.darkThemeConfig == DarkThemeConfig.DARK,
                onClick = { onChangeDarkThemeConfig(DarkThemeConfig.DARK) },
            )
        }
    }
}

@Composable
private fun BooleanSettings(
    title: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    SettingsDialogSectionTitle(text = title)
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(Res.string.settings_boolean_true),
            selected = value,
            onClick = { onValueChange(true) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(Res.string.settings_boolean_false),
            selected = !value,
            onClick = { onValueChange(false) },
        )
    }
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}


