package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import dev.johnoreilly.confetti.GeminiApi
import dev.johnoreilly.confetti.decompose.RecommendationsComponent
import dev.johnoreilly.confetti.preview.MobilePreviews
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@Composable
fun GeminiQueryView(component: RecommendationsComponent) {
    val sessionList by component.sessions.collectAsState(emptyList())
    var conferenceInfo by remember { mutableStateOf("") }

    LaunchedEffect(component) {
        conferenceInfo = "Speakers, Session ID, Title, Start Time, Description,\n"
        sessionList.forEach { session ->
            session.sessionDescription?.let {
                val speakers = session.speakers.joinToString(" ") { it.speakerDetails.name }
                conferenceInfo += "${speakers}, ${session.id}, ${session.title}, ${session.startsAt}, ${session.sessionDescription}, \n"
            }
        }
    }

    RecommendationsView(conferenceInfo)
}


@Composable
fun RecommendationsView(conferenceInfo: String) {
    // Previews (LocalInspectionMode = true) can't create a GeminiApi without
    // an API key from BuildKonfig; skip the instance entirely in inspection mode.
    val inspecting = LocalInspectionMode.current
    val geminiApi = remember(inspecting) { if (inspecting) null else GeminiApi() }
    var query by remember { mutableStateOf("") }
    var queryResponse by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    var showProgress by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Enter query") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    if (query.isNotEmpty() && geminiApi != null) {
                        coroutineScope.launch {
                            val prompt = "$query. Base on the following CSV: $conferenceInfo}"
                            queryResponse = ""
                            geminiApi.generateContentStream(prompt)
                                .catch {
                                    showProgress = false
                                    queryResponse = it.message ?: "Error making gemini request"
                                }
                                .onStart { showProgress = true }
                                .collect {
                                    showProgress = false
                                    println(it.text)
                                    queryResponse += it.text
                                }

                        }
                    }
                }
            ),
            shape = ShapeDefaults.Large,
            singleLine = true,
        )

        Spacer(Modifier.height(16.dp))

        if (showProgress) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Markdown(queryResponse)
        }
    }
}

@MobilePreviews
@Composable
internal fun RecommendationsViewLoadedPreview() {
    RecommendationsView(
        conferenceInfo = "Speakers, Session ID, Title, Start Time, Description,\n" +
            "John O'Reilly, 368995, Confetti talk, 2023-04-13T14:00, KMM live coding\n",
    )
}

@Preview(name = "Empty conference info", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun RecommendationsViewEmptyPreview() {
    RecommendationsView(conferenceInfo = "")
}
