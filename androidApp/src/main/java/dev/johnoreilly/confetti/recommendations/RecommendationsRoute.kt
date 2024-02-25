package dev.johnoreilly.confetti.recommendations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.decompose.RecommendationsComponent
import dev.johnoreilly.confetti.sessions.SessionItemView
import dev.johnoreilly.confetti.ui.ConfettiTypography
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.HomeScaffold
import dev.johnoreilly.confetti.ui.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsRoute(
    component: RecommendationsComponent,
    windowSizeClass: WindowSizeClass,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var query by remember { mutableStateOf("") }

    val uiState by component.uiState.subscribeAsState()

    val bookmarks by component.bookmarks
        .collectAsStateWithLifecycle(initialValue = emptySet())


    HomeScaffold(
        title = stringResource(R.string.recommendations),
        windowSizeClass = windowSizeClass,
        topBarActions = topBarActions,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(id = R.string.recommendations_search_placeholder)) },
                leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        if (query.isNotEmpty()) {
                            component.makeQuery(query)
                        }
                    }
                ),
                textStyle = ConfettiTypography.bodyLarge,
                shape = ShapeDefaults.Large,
                singleLine = true,
            )

            Spacer(Modifier.height(16.dp))

            when (val uiState1 = uiState) {
                is RecommendationsComponent.Success -> {
                    RecommendationsView(uiState = uiState1,
                        bookmarks = bookmarks,
                        addBookmark = component::addBookmark,
                        removeBookmark = component::removeBookmark,
                        navigateToSession = component::onSessionClicked)
                }
                is RecommendationsComponent.Loading -> { LoadingView() }
                is RecommendationsComponent.Error -> { ErrorView() }
                RecommendationsComponent.Initial -> {}
            }
        }
    }
}

@Composable
fun RecommendationsView(
    uiState: RecommendationsComponent.Success,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    navigateToSession: (id: String) -> Unit
) {
    Column {
        uiState.data.recommendedSessions.forEach { session ->
            Row {
                SessionItemView(
                    session = session,
                    sessionSelected = navigateToSession,
                    isBookmarked = false, //bookmarks.contains(session.id),
                    addBookmark = {  },
                    removeBookmark = {  },
                    onNavigateToSignIn = { },
                    isLoggedIn = false,
                )
            }
        }
    }
}