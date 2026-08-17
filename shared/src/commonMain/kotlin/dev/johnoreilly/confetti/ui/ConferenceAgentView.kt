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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.AsyncImage
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.animation.core.*
import dev.johnoreilly.confetti.ui.component.FullScreenPhotoDialog
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
fun ConferenceAgentView(
    component: ConferenceAgentComponent,
    onCloseClick: () -> Unit,
    bottomContentPadding: Dp = 0.dp
) {
    val state by component.uiState.subscribeAsState()
    val listState = rememberLazyListState()
    val renderMessages = remember(state.messages) { groupMessages(state.messages) }
    var showPhotoDialogUrl by remember { mutableStateOf<String?>(null) }
    val hazeState = remember { HazeState() }
    val systemBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val totalBottomPadding = systemBottomPadding + bottomContentPadding
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    var inputBarHeightDp by remember { mutableStateOf(88.dp + totalBottomPadding) }
    val isAtBottom by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                true
            } else {
                val lastVisible = visibleItems.last()
                val isLastItem = lastVisible.index >= layoutInfo.totalItemsCount - 2
                val viewportEnd = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
                val isFullyVisible = (lastVisible.offset + lastVisible.size) <= viewportEnd
                isLastItem && (isFullyVisible || !listState.canScrollForward)
            }
        }
    }

    var shouldAutoScroll by remember { mutableStateOf(true) }

    LaunchedEffect(listState.isScrollInProgress, isAtBottom) {
        if (listState.isScrollInProgress) {
            shouldAutoScroll = isAtBottom
        } else if (isAtBottom) {
            shouldAutoScroll = true
        }
    }

    val targetIndex = if (state.isLoading) renderMessages.size else renderMessages.lastIndex

    val onSend = {
        if (state.inputText.isNotBlank()) {
            shouldAutoScroll = true
            component.sendMessage()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(renderMessages.size, state.isLoading, inputBarHeightDp) {
        if (targetIndex >= 0 && shouldAutoScroll) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Assistant",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { component.restartChat() }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Clear Chat",
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = inputBarHeightDp + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(renderMessages) { renderMessage ->
                    when (renderMessage) {
                        is RenderMessage.Single -> MessageBubble(
                            message = renderMessage.message,
                            userPhotoUrl = state.userPhotoUrl,
                            onPhotoClick = { url -> showPhotoDialogUrl = url }
                        )
                        is RenderMessage.ToolCallGroup -> ToolCallGroupBubble(renderMessage)
                    }
                }

                if (state.isLoading) {
                    item {
                        TypingBubble()
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { size ->
                        with(density) {
                            inputBarHeightDp = size.height.toDp()
                        }
                    }
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            tint = HazeTint(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                            blurRadius = 25.dp,
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(bottom = totalBottomPadding),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    TextField(
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        value = state.inputText,
                        onValueChange = component::updateInputText,
                        placeholder = { Text(stringResource(Res.string.agent_placeholder)) },
                        enabled = state.isInputEnabled && !state.isChatEnded,
                        minLines = 1,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default,
                        ),
                        shape = ShapeDefaults.Large,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = onSend,
                        enabled = state.isInputEnabled &&
                            !state.isChatEnded &&
                            state.inputText.isNotBlank(),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = stringResource(Res.string.agent_send),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

        if (showPhotoDialogUrl != null) {
            FullScreenPhotoDialog(
                photoUrl = showPhotoDialogUrl,
                contentDescription = "User Profile Photo",
                onDismissRequest = { showPhotoDialogUrl = null }
            )
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
private fun MessageBubble(
    message: ConferenceAgentComponent.Message,
    userPhotoUrl: String?,
    onPhotoClick: (String) -> Unit
) {
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
                    .weight(1f, fill = false)
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
                if (userPhotoUrl != null) {
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = "User Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onPhotoClick(userPhotoUrl) }
                    )
                } else {
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
}

@Composable
private fun ToolCallGroupBubble(group: RenderMessage.ToolCallGroup) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { expanded = !expanded }
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
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
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                group.toolCalls.forEach { toolCall ->
                    Text(
                        text = toolCall.text,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
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

@Composable
private fun TypingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        MessageAvatar(
            imageVector = Icons.Filled.Assistant,
            contentDescription = "Agent",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            TypingIndicator()
        }
    }
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition()
    val alphas = (0..2).map { index ->
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 600
                    0.2f at 0 with LinearEasing
                    1.0f at 200 with LinearEasing
                    0.2f at 400 with LinearEasing
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 150)
            )
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        alphas.forEach { alphaState ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alphaState.value),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}
