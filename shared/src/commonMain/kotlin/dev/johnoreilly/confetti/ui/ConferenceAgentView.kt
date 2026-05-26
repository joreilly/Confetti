package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mikepenz.markdown.m3.Markdown
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.agent_placeholder
import confetti.shared.generated.resources.agent_restart
import confetti.shared.generated.resources.agent_send
import dev.johnoreilly.confetti.decompose.ConferenceAgentComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConferenceAgentView(component: ConferenceAgentComponent) {
    val state by component.uiState.subscribeAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { component.restartChat() }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(Res.string.agent_restart),
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages) { message ->
                MessageBubble(message)
            }

            if (state.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.inputText,
                onValueChange = component::updateInputText,
                placeholder = { Text(stringResource(Res.string.agent_placeholder)) },
                enabled = state.isInputEnabled && !state.isChatEnded,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { component.sendMessage() }),
                shape = RoundedCornerShape(24.dp),
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = { component.sendMessage() },
                enabled = state.isInputEnabled &&
                    !state.isChatEnded &&
                    state.inputText.isNotBlank(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = stringResource(Res.string.agent_send),
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ConferenceAgentComponent.Message) {
    val alignment = when (message) {
        is ConferenceAgentComponent.Message.User -> Alignment.End
        else -> Alignment.Start
    }
    val background = when (message) {
        is ConferenceAgentComponent.Message.User -> MaterialTheme.colorScheme.primaryContainer
        is ConferenceAgentComponent.Message.Agent -> MaterialTheme.colorScheme.surfaceVariant
        is ConferenceAgentComponent.Message.System -> Color.Transparent
        is ConferenceAgentComponent.Message.ToolCall -> Color.Transparent
        is ConferenceAgentComponent.Message.Error -> MaterialTheme.colorScheme.errorContainer
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .align(alignment)
                .widthIn(max = 480.dp)
                .background(background, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            when (message) {
                is ConferenceAgentComponent.Message.Agent -> Markdown(message.text)
                is ConferenceAgentComponent.Message.System ->
                    Text(message.text, fontStyle = FontStyle.Italic)
                is ConferenceAgentComponent.Message.ToolCall ->
                    Text(
                        text = "🔧 ${message.text}",
                        color = MaterialTheme.colorScheme.outline,
                        fontStyle = FontStyle.Italic,
                    )
                is ConferenceAgentComponent.Message.Error ->
                    Text(message.text, color = MaterialTheme.colorScheme.onErrorContainer)
                is ConferenceAgentComponent.Message.User -> Text(message.text)
            }
        }
    }
}
