@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class,
    ExperimentalPagerApi::class
)
@file:Suppress("FunctionName")

package dev.johnoreilly.confetti.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.sessionSpeakerLocation
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.ui.component.pagerTabIndicatorOffset
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel


@Composable
fun ConfettiApp(viewModel: ConfettiViewModel = getViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SessionListScreen(uiState)
}


@Composable
fun SessionListScreen(uiState: SessionsUiState) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState is SessionsUiState.Success) uiState.conferenceName else "") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            when (uiState) {
                SessionsUiState.Loading ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)) {
                        CircularProgressIndicator()
                    }

                is SessionsUiState.Success -> {
                    SessionListView(uiState)
                }
            }
        }
    }
}


@Composable
fun SessionListView(uiState: SessionsUiState.Success) {
    val pagerState = rememberPagerState()

    Column {
        SessionListTabRow(pagerState, uiState)

        HorizontalPager(
            count = uiState.confDates.size,
            state = pagerState,
        ) { page ->

            val sessions = uiState.sessionsByStartTimeList[page]
            LazyColumn {
                sessions.forEach {
                    item {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                it.key,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Divider()
                        }
                    }

                    items(it.value) { session ->
                        SessionView(session)
                    }
                }
            }
        }
    }
}

@Composable
fun SessionListTabRow(pagerState: PagerState, uiState: SessionsUiState.Success) {
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }
    ) {
        uiState.confDates.forEachIndexed { index, date ->
            val coroutineScope = rememberCoroutineScope()

            ConfettiTab(
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = { Text(text = date.toString()) }
            )
        }
    }

}

@Composable
fun SessionView(session: SessionDetails) {
    ListItem(
        headlineText = { Text(session.title) },
        supportingText = { Text(session.sessionSpeakerLocation(), fontWeight = FontWeight.Bold) }
    )
}