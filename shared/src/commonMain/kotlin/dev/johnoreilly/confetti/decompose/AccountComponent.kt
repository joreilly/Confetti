package dev.johnoreilly.confetti.decompose

import com.apollographql.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.BuildKonfig
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import kotlinx.datetime.LocalDate

interface AccountComponent {
    val conference: String
    val user: User?
    val uiState: Value<AccountUiState>

    fun onVenueClicked()
    fun onSwitchConferenceClicked()
    fun onSettingsClicked()
    fun onSignInClicked()
    fun onSignOutClicked()
}

sealed interface AccountUiState {
    data object Loading : AccountUiState
    data class Success(
        val conferenceName: String,
        val conferenceDays: String,
        val user: User?,
        val hasVenue: Boolean = true,
    ) : AccountUiState
    data object Error : AccountUiState
}

class DefaultAccountComponent(
    componentContext: ComponentContext,
    override val conference: String,
    override val user: User?,
    private val onVenueSelected: () -> Unit,
    private val onSwitchConference: () -> Unit,
    private val onShowSettings: () -> Unit,
    private val onSignIn: () -> Unit,
    private val onSignOut: () -> Unit,
) : AccountComponent, KoinComponent, ComponentContext by componentContext {

    private val repository: ConfettiRepository by inject()
    private val coroutineScope = coroutineScope()

    private val _uiState = MutableValue<AccountUiState>(AccountUiState.Loading)
    override val uiState: Value<AccountUiState> = _uiState

    init {
        coroutineScope.launch {
            try {
                val response = repository.conferenceData(conference, FetchPolicy.CacheFirst)
                val config = response.data?.config
                val venues = response.data?.venues
                val daysStr = config?.days?.let { days ->
                    formatConferenceDates(days)
                }.orEmpty()

                _uiState.value = AccountUiState.Success(
                    conferenceName = config?.name ?: conference,
                    conferenceDays = daysStr,
                    user = user,
                    hasVenue = !venues.isNullOrEmpty(),
                )
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error
            }
        }
    }

    private fun formatConferenceDates(days: List<LocalDate>): String {
        if (days.isEmpty()) return ""
        val start = days.first()
        val end = days.last()
        val startMonth = start.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val endMonth = end.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        return if (days.size == 1) {
            "$startMonth ${start.dayOfMonth}, ${start.year}"
        } else if (start.month == end.month && start.year == end.year) {
            "$startMonth ${start.dayOfMonth} - ${end.dayOfMonth}, ${start.year}"
        } else if (start.year == end.year) {
            "$startMonth ${start.dayOfMonth} - $endMonth ${end.dayOfMonth}, ${start.year}"
        } else {
            "$startMonth ${start.dayOfMonth}, ${start.year} - $endMonth ${end.dayOfMonth}, ${end.year}"
        }
    }

    override fun onVenueClicked() = onVenueSelected()
    override fun onSwitchConferenceClicked() = onSwitchConference()
    override fun onSettingsClicked() = onShowSettings()
    override fun onSignInClicked() = onSignIn()
    override fun onSignOutClicked() = onSignOut()
}
