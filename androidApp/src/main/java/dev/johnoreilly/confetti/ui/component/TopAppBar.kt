package dev.johnoreilly.confetti.ui.component

import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfettiTopAppBar(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors()
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        colors = colors,
        modifier = modifier
    )
}

