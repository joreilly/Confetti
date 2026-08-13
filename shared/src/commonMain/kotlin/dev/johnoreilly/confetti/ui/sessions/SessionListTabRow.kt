package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import dev.johnoreilly.confetti.decompose.SessionsUiState
import kotlinx.coroutines.launch

@Composable
fun SessionListTabRow(pagerState: PagerState, uiState: SessionsUiState.Success) {
    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage, matchContentSize = true),
            )
        },
        divider = {},
    ) {
        uiState.formattedConfDates.forEachIndexed { index, formattedDate ->
            val coroutineScope = rememberCoroutineScope()
            val selected = pagerState.currentPage == index

            Tab(
                selected = selected,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            )
        }
    }
}