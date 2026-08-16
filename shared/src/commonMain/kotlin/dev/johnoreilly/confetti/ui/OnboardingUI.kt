package dev.johnoreilly.confetti.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.johnoreilly.confetti.decompose.OnboardingComponent
import dev.johnoreilly.confetti.permissions.rememberNotificationPermissionState
import dev.johnoreilly.confetti.ui.component.ConfettiAlertDialog
import kotlinx.coroutines.launch

@Composable
fun OnboardingUI(component: OnboardingComponent) {
    ConferenceMaterialThemeFromSettings(seedColorString = null) {
        val notificationsEnabled by component.notificationsEnabled.collectAsState(initial = false)
        var showPermissionDeniedDialog by remember { mutableStateOf(false) }

        val notificationPermissionState = rememberNotificationPermissionState(
            notificationsActive = notificationsEnabled,
            onPermissionDeniedAlways = {
                showPermissionDeniedDialog = true
            },
            onPermissionStatus = { hasPermission ->
                if (notificationsEnabled != hasPermission) {
                    component.updateNotificationsEnabled(hasPermission)
                }
            }
        )

        if (showPermissionDeniedDialog) {
            ConfettiAlertDialog(
                title = "Permission Required",
                text = "Notification permission was permanently denied. Please enable it in Settings to receive updates.",
                confirmText = "Open Settings",
                onConfirm = {
                    showPermissionDeniedDialog = false
                    notificationPermissionState.openSettings()
                },
                dismissText = "Cancel",
                onDismiss = { showPermissionDeniedDialog = false }
            )
        }

        OnboardingContent(
            notificationsEnabled = notificationsEnabled,
            supportsNotifications = component.supportsNotifications,
            onNotificationsToggle = { enabled ->
                if (enabled) {
                    notificationPermissionState.maybeRequest()
                } else {
                    component.updateNotificationsEnabled(false)
                }
            },
            onComplete = component::completeOnboarding,
        )
    }
}

@Composable
fun OnboardingContent(
    notificationsEnabled: Boolean,
    supportsNotifications: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onComplete: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { 3 }

    // Infinite transition to create a slowly moving gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "GradientAnimation")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "xOffset"
    )
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
        ),
        start = Offset(x = xOffset, y = 0f),
        end = Offset(x = 1000f - xOffset, y = yOffset)
    )

    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated moving gradient background container with haze modifier
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .hazeSource(hazeState)
        )

        // Scaffold as a sibling layout to prevent nested descendant error
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Skip Button Row
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage < 2) {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }) {
                            Text("Skip")
                        }
                    }
                }

                // Glassmorphic Card (sibling of background haze modifier)
                val cardShape = MaterialTheme.shapes.large
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clip(cardShape)
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = Color.Transparent,
                                tint = HazeTint(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)),
                                blurRadius = 30.dp,
                            )
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = cardShape
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> OnboardingStep1(visible = pagerState.currentPage == 0)
                            1 -> OnboardingStep2(visible = pagerState.currentPage == 1)
                            2 -> OnboardingStep3(
                                visible = pagerState.currentPage == 2,
                                notificationsEnabled = notificationsEnabled,
                                supportsNotifications = supportsNotifications,
                                onNotificationsToggle = onNotificationsToggle,
                            )
                        }
                    }
                }

                // Bottom Navigation & Progress Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    if (pagerState.currentPage > 0) {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(64.dp))
                    }

                    // Smooth Dot Indicators (Clickable)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (step in 0..2) {
                            val isActive = pagerState.currentPage == step
                            val width by animateDpAsState(targetValue = if (isActive) 24.dp else 8.dp)
                            val color = if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(width = width, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(step)
                                        }
                                    }
                            )
                        }
                    }

                    // Next or Done Button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onComplete()
                            }
                        }
                    ) {
                        Text(if (pagerState.currentPage == 2) "Done" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStep1(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 24.dp)
            )
            Text(
                text = "Welcome to Confetti",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Your conference companion. Browse schedules, speakers, and venues offline.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun OnboardingStep2(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bookmarks,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 24.dp)
            )
            Text(
                text = "Features",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                FeatureRow("Bookmark sessions to build schedule")
                FeatureRow("Get answers from the AI assistant")
                FeatureRow("View speakers and venue maps")
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OnboardingStep3(
    visible: Boolean,
    notificationsEnabled: Boolean,
    supportsNotifications: Boolean,
    onNotificationsToggle: (Boolean) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 24.dp)
            )
            Text(
                text = "Stay Updated",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Enable notifications to receive session reminders. You can disable this anytime in settings.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            )
            if (supportsNotifications) {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(if (notificationsEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onNotificationsToggle(!notificationsEnabled) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = null
                    )
                    Text(
                        text = "Enable notifications",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (notificationsEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
