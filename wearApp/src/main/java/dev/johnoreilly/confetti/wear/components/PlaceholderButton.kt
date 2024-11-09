package dev.johnoreilly.confetti.wear.components;

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ActiveFocusListener
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonColors
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.PlaceholderDefaults
import androidx.wear.compose.material3.PlaceholderState
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import kotlinx.coroutines.launch

/**
 * A placeholder chip to be displayed while the contents of the [Chip] is being loaded.
 */
@Composable
public fun PlaceholderButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    placeholderState: PlaceholderState = rememberActivePlaceholderState { false },
    secondaryLabel: Boolean = true,
    icon: Boolean = true,
    colors: ButtonColors = PlaceholderDefaults.placeholderButtonColors(placeholderState),
    enabled: Boolean = false,
) {
    Button(
        modifier = modifier
            .height(ButtonDefaults.Height)
            .fillMaxWidth()
            .placeholderShimmer(placeholderState),
        onClick = onClick,
        enabled = enabled,
        label = {
            Column {
                Box(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .height(12.dp)
                        .placeholder(placeholderState),
                )
                Spacer(Modifier.size(8.dp))
            }
        },
        secondaryLabel = if (secondaryLabel) {
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 30.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .height(12.dp)
                        .placeholder(placeholderState),
                )
            }
        } else {
            null
        },
        icon = if (icon) {
            {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(ButtonDefaults.LargeIconSize)
                        .placeholder(placeholderState),
                )
            }
        } else {
            null
        },
        colors = colors,
    )
}

@Composable
fun rememberActivePlaceholderState(isContentReady: () -> Boolean): PlaceholderState {
    val placeholderState = rememberPlaceholderState {
        isContentReady()
    }

    ActiveFocusListener { focused ->
        if (focused) {
            if (placeholderState.isHidden) {
                launch {
                    placeholderState.animatePlaceholder()
                }
            }
        }
    }

    return placeholderState
}