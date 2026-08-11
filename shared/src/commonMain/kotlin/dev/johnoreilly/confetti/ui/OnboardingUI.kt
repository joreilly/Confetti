package dev.johnoreilly.confetti.ui

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.decompose.OnboardingComponent
import dev.johnoreilly.confetti.permissions.rememberNotificationPermissionState
import dev.johnoreilly.confetti.ui.component.ConfettiAlertDialog
import kotlinx.coroutines.launch

@Composable
fun OnboardingUI(component: OnboardingComponent) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { 3 }
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

    Scaffold { padding ->
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

            // Main Onboarding Card containing HorizontalPager
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (page) {
                            0 -> OnboardingStep1()
                            1 -> OnboardingStep2()
                            2 -> OnboardingStep3(
                                notificationsEnabled = notificationsEnabled,
                                supportsNotifications = component.supportsNotifications,
                                onNotificationsToggle = { enabled ->
                                    if (enabled) {
                                        notificationPermissionState.maybeRequest()
                                    } else {
                                        component.updateNotificationsEnabled(false)
                                    }
                                }
                            )
                        }
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
                            component.completeOnboarding()
                        }
                    }
                ) {
                    Text(if (pagerState.currentPage == 2) "Done" else "Next")
                }
            }
        }
    }
}

@Composable
fun OnboardingStep1() {
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

@Composable
fun OnboardingStep2() {
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
    notificationsEnabled: Boolean,
    supportsNotifications: Boolean,
    onNotificationsToggle: (Boolean) -> Unit
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
