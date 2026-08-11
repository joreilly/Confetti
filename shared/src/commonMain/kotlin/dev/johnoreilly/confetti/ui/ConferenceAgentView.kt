package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
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
fun ConferenceAgentView(component: ConferenceAgentComponent, bottomContentPadding: Dp = 0.dp) {
    val state by component.uiState.subscribeAsState()
    val listState = rememberLazyListState()
    val renderMessages = remember(state.messages) { groupMessages(state.messages) }

    LaunchedEffect(renderMessages.size) {
        if (renderMessages.isNotEmpty()) {
            listState.animateScrollToItem(renderMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp)
            .padding(bottom = bottomContentPadding)
    ) {
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
            items(renderMessages) { renderMessage ->
                when (renderMessage) {
                    is RenderMessage.Single -> MessageBubble(renderMessage.message)
                    is RenderMessage.ToolCallGroup -> ToolCallGroupBubble(renderMessage)
                }
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
private fun MessageAvatar(
    imageVector: ImageVector,
    contentDescription: String,
    containerColor: Color,
    iconColor: Color
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(containerColor, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MessageBubble(message: ConferenceAgentComponent.Message) {
    val isSystem = message is ConferenceAgentComponent.Message.System
    val isUser = message is ConferenceAgentComponent.Message.User
    val isAgent = message is ConferenceAgentComponent.Message.Agent
    val isError = message is ConferenceAgentComponent.Message.Error

    val background = when (message) {
        is ConferenceAgentComponent.Message.User -> MaterialTheme.colorScheme.primaryContainer
        is ConferenceAgentComponent.Message.Agent -> MaterialTheme.colorScheme.surfaceVariant
        is ConferenceAgentComponent.Message.System -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        is ConferenceAgentComponent.Message.ToolCall -> Color.Transparent
        is ConferenceAgentComponent.Message.Error -> MaterialTheme.colorScheme.errorContainer
    }

    if (isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(background, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = message.text,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser && (isAgent || isError)) {
                MessageAvatar(
                    imageVector = Icons.Filled.Assistant,
                    contentDescription = "Agent",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 660.dp)
                    .background(background, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                when (message) {
                    is ConferenceAgentComponent.Message.Agent -> Markdown(message.text)
                    is ConferenceAgentComponent.Message.Error ->
                        Text(message.text, color = MaterialTheme.colorScheme.onErrorContainer)
                    is ConferenceAgentComponent.Message.User -> Text(message.text)
                    is ConferenceAgentComponent.Message.ToolCall ->
                        Text(
                            text = "🔧 ${message.text}",
                            color = MaterialTheme.colorScheme.outline,
                            fontStyle = FontStyle.Italic,
                        )
                    is ConferenceAgentComponent.Message.System -> {}
                }
            }

            if (isUser) {
                Spacer(Modifier.width(8.dp))
                MessageAvatar(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User",
                    containerColor = MaterialTheme.colorScheme.primary,
                    iconColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ToolCallGroupBubble(group: RenderMessage.ToolCallGroup) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "🔧 Called ${group.toolCalls.size} tools",
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.outline
            )
        }

        if (expanded) {
            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                group.toolCalls.forEach { toolCall ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = toolCall.text,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

private sealed interface RenderMessage {
    data class Single(val message: ConferenceAgentComponent.Message) : RenderMessage
    data class ToolCallGroup(val toolCalls: List<ConferenceAgentComponent.Message.ToolCall>) : RenderMessage
}

private fun groupMessages(messages: List<ConferenceAgentComponent.Message>): List<RenderMessage> {
    val result = mutableListOf<RenderMessage>()
    val currentToolCalls = mutableListOf<ConferenceAgentComponent.Message.ToolCall>()

    for (message in messages) {
        if (message is ConferenceAgentComponent.Message.ToolCall) {
            currentToolCalls.add(message)
        } else {
            if (currentToolCalls.isNotEmpty()) {
                result.add(RenderMessage.ToolCallGroup(currentToolCalls.toList()))
                currentToolCalls.clear()
            }
            result.add(RenderMessage.Single(message))
        }
    }
    if (currentToolCalls.isNotEmpty()) {
        result.add(RenderMessage.ToolCallGroup(currentToolCalls.toList()))
    }
    return result
}
