package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.work.NotificationSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface OnboardingComponent {
    val supportsNotifications: Boolean
    val notificationsEnabled: Flow<Boolean>
    val currentUser: Flow<User?>

    fun updateNotificationsEnabled(enabled: Boolean)
    fun onSignInClicked()
    fun completeOnboarding()
}

class DefaultOnboardingComponent(
    componentContext: ComponentContext,
    private val onSignInRequested: () -> Unit,
    private val onFinished: () -> Unit,
) : OnboardingComponent, KoinComponent, ComponentContext by componentContext {

    private val appSettings: AppSettings by inject()
    private val notificationSender: NotificationSender? by inject()
    private val authentication: Authentication by inject()
    private val coroutineScope = coroutineScope()

    override val supportsNotifications: Boolean
        get() = notificationSender != null

    override val notificationsEnabled: Flow<Boolean>
        get() = appSettings.notificationsEnabledFlow

    override val currentUser: Flow<User?>
        get() = authentication.currentUser

    override fun updateNotificationsEnabled(enabled: Boolean) {
        coroutineScope.launch {
            appSettings.setNotificationsEnabled(enabled)
            notificationSender?.updateSchedule(enabled)
        }
    }

    override fun onSignInClicked() {
        onSignInRequested()
    }

    override fun completeOnboarding() {
        coroutineScope.launch {
            appSettings.setOnboardingCompleted(true)
            onFinished()
        }
    }
}
