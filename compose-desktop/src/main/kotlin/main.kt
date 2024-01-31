import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.apollographql.apollo3.ApolloClient
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.dsl.module

private fun mainModule() = module {
    factory {
        ApolloClient.Builder()
            .serverUrl("https://confetti-app.dev/graphql")
    }
}

val koin = initKoin {
    modules(mainModule())
}.koin

fun main() = application {
    val windowState = rememberWindowState()

    LaunchedEffect(key1 = this) {
        Napier.base(DebugAntilog())
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Confetti"
    ) {
        MaterialTheme() {
            MainLayout()
        }
    }
}


@Composable
fun MainLayout() {
    var conference by remember { mutableStateOf<GetConferencesQuery.Conference?>(null) }

    conference?.let {
        ConferenceView(it) {
            conference = null
        }
    } ?: run {
        ConferenceListView {
            conference = it
        }
    }
}


@Composable
fun ConfettiHeader(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Divider()
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon?.let { icon ->
                    Icon(
                        modifier = Modifier
                            .padding(end = 8.dp),
                        imageVector = icon,
                        contentDescription = null,
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Divider()
        }
    }
}

