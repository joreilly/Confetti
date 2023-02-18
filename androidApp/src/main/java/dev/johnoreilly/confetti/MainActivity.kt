@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)

package dev.johnoreilly.confetti

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.johnoreilly.confetti.ui.ConfettiTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.ui.component.SessionListTabRow
import dev.johnoreilly.confetti.ui.component.pagerTabIndicatorOffset
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConfettiTheme {
                SessionListScreen()
            }
        }
    }
}

@Composable
fun SessionListScreen(viewModel: ConfettiViewModel = getViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        SessionsUiState.Loading ->
            Column(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                CircularProgressIndicator()
            }

        is SessionsUiState.Success -> {
            SessionListView(state)
        }
    }
}


@Composable
fun SessionListView(uiState: SessionsUiState.Success) {
    val pagerState = rememberPagerState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(uiState.conferenceName) }) }
    ) {
        Column(Modifier.padding(it)) {
            SessionListTabRow(pagerState, uiState)


            HorizontalPager(count = uiState.confDates.size, state = pagerState,
            ) { page ->

                val sessionsMap = uiState.sessionsByStartTimeList[page]
                LazyColumn {
                    sessionsMap.forEach { sessions ->

                        item {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(sessions.key, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Divider()
                            }
                        }

                        items(sessions.value) { session ->
                            SessionView(session)
                        }
                    }
                }
            }

        }
    }

}

@Composable
fun SessionView(session: SessionDetails) {
    ListItem(
        headlineText = { Text(session.title) },
        supportingText = { Text(session.sessionSpeakerInfo(), fontWeight = FontWeight.Bold) }
    )
}
