@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.speakers
import dev.johnoreilly.confetti.GeminiApi
import dev.johnoreilly.confetti.decompose.RecommendationsComponent
import dev.johnoreilly.confetti.decompose.SpeakersUiState
import dev.johnoreilly.confetti.decompose.iosPromptApi
import dev.johnoreilly.confetti.prompt.PromptApi
import dev.johnoreilly.confetti.ui.component.ErrorView
import dev.johnoreilly.confetti.ui.component.LoadingView
import dev.johnoreilly.confetti.ui.speakers.SpeakerGridView
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.component.inject
import kotlin.getValue

@Composable
fun GeminiQueryView(component: RecommendationsComponent) {
    val sessionList by component.sessions.collectAsState(emptyList())
    var conferenceInfo by remember { mutableStateOf("") }

    LaunchedEffect(sessionList) {
        conferenceInfo = "" //""Title\n"
        sessionList.forEach { session ->
            session.sessionDescription?.let {
                //val speakers = session.speakers.joinToString(" ") { it.speakerDetails.name }
                conferenceInfo += "${session.title},\n"
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("AI query") })
        }
    ) {
        Column(Modifier.padding(it)) {
            RecommendationsView(conferenceInfo)
        }
    }



}


@Composable
fun RecommendationsView(conferenceInfo: String) {
    //val geminiApi = remember { GeminiApi() }
    val promptApi = koinInject<PromptApi>()
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
                    if (query.isNotEmpty()) {
                        coroutineScope.launch {
                            val prompt = "$query. Base on the following list of conference sessions: $conferenceInfo}"
                            //queryResponse = ""


                            queryResponse = iosPromptApi?.generateContent(prompt, query)?.text ?: "Error making gemini request"

//                            promptApi.generateContentStream(prompt)
//                                .catch {
//                                    showProgress = false
//                                    queryResponse = it.message ?: "Error making gemini request"
//                                }
//                                .onStart { showProgress = true }
//                                .collect {
//                                    showProgress = false
//                                    println(it.text)
//                                    queryResponse += it.text
//                                }

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
