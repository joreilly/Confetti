package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/** A deterministic backdrop that makes the real translucent [HazeBottomBar] visible in previews. */
@Composable
fun HazeBottomBarPreviewContent() {
    HazeBottomBarPreviewLayout(showContrastTarget = false)
}

/**
 * A close-up fixture with hard edges and oversized text immediately behind the bar. The sharp
 * reference above the bar and the softened continuation below it make blur distinct from tint.
 */
@Composable
fun HazeBottomBarContrastPreviewContent() {
    HazeBottomBarPreviewLayout(showContrastTarget = true)
}

@Composable
private fun HazeBottomBarPreviewLayout(showContrastTarget: Boolean) {
    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("KotlinConf 2026", style = MaterialTheme.typography.headlineMedium)
                repeat(7) { index ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${10 + index}:00", color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text("A session card scrolling behind the navigation bar")
                        }
                    }
                }
            }

            if (showContrastTarget) {
                HazeContrastTarget(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(150.dp),
                )
            }
        }

        HazeBottomBar(
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            PreviewNavigationItem(
                selected = true,
                label = "Schedule",
                selectedIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                unselectedIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
            )
            PreviewNavigationItem(
                selected = false,
                label = "Speakers",
                selectedIcon = { Icon(Icons.Filled.People, contentDescription = null) },
                unselectedIcon = { Icon(Icons.Outlined.People, contentDescription = null) },
            )
            PreviewNavigationItem(
                selected = false,
                label = "Bookmarks",
                selectedIcon = { Icon(Icons.Filled.Bookmarks, contentDescription = null) },
                unselectedIcon = { Icon(Icons.Outlined.Bookmarks, contentDescription = null) },
            )
            PreviewNavigationItem(
                selected = false,
                label = "Account",
                selectedIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                unselectedIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun HazeContrastTarget(modifier: Modifier = Modifier) {
    Box(modifier) {
        Row(modifier = Modifier.fillMaxSize()) {
            val colors = listOf(
                Color(0xFF101010),
                Color(0xFFF8F8F8),
                Color(0xFFFF3155),
                Color(0xFF00C8FF),
            )
            repeat(16) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(colors[index % colors.size]),
                )
            }
        }
        Text(
            text = "SHARP  HAZE  BLUR",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            color = Color.Black,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

@Composable
private fun RowScope.PreviewNavigationItem(
    selected: Boolean,
    label: String,
    selectedIcon: @Composable () -> Unit,
    unselectedIcon: @Composable () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = {},
        icon = {
            Box(modifier = Modifier.size(24.dp)) {
                if (selected) selectedIcon() else unselectedIcon()
            }
        },
        label = { Text(label) },
    )
}
