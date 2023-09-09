package dev.johnoreilly.confetti.wear.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.push
import dev.johnoreilly.confetti.decompose.DefaultConferencesComponent
import dev.johnoreilly.confetti.decompose.DefaultSessionDetailsComponent
import dev.johnoreilly.confetti.decompose.DefaultSpeakerDetailsComponent
import dev.johnoreilly.confetti.wear.auth.DefaultFirebaseSignOutComponent
import dev.johnoreilly.confetti.wear.bookmarks.DefaultBookmarksComponent
import dev.johnoreilly.confetti.wear.home.DefaultHomeComponent
import dev.johnoreilly.confetti.wear.sessions.DefaultConferenceSessionsComponent
import dev.johnoreilly.confetti.wear.settings.DefaultSettingsComponent
import kotlinx.coroutines.launch


fun DefaultWearAppComponent.buildChild(config: Config, componentContext: ComponentContext): Child =
    when (config) {
        is Config.Loading -> Child.Loading

        is Config.Conferences ->
            Child.Conferences(
                DefaultConferencesComponent(
                    componentContext = componentContext,
                    onConferenceSelected = { conference ->
                        coroutineScope.launch {
                            repository.setConference(conference = conference.id)
                        }
                        showConference(conference = conference.id)
                    },
                )
            )

        is Config.ConferenceSessions ->
            Child.ConferenceSessions(
                DefaultConferenceSessionsComponent(
                    componentContext = componentContext,
                    conference = config.conference,
                    date = config.date,
                    user = user,
                    onSessionSelected = {
                        navigation.push(Config.SessionDetails(user?.uid, config.conference, it))
                    }
                )
            )

        is Config.SessionDetails ->
            Child.SessionDetails(
                DefaultSessionDetailsComponent(
                    componentContext = componentContext,
                    conference = config.conference,
                    sessionId = config.session,
                    onSpeakerSelected = {
                        navigation.push(Config.SpeakerDetails(user?.uid, config.conference, it))
                    },
                    user = user
                )
            )

        is Config.SpeakerDetails ->
            Child.SpeakerDetails(
                DefaultSpeakerDetailsComponent(
                    componentContext = componentContext,
                    conference = config.conference,
                    speakerId = config.speaker,
                    onSessionSelected = {
                        navigation.push(Config.SpeakerDetails(user?.uid, config.conference, it))
                    },
                )
            )

        is Config.Settings -> Child.Settings(
            DefaultSettingsComponent(
                componentContext,
                onNavigateToGoogleSignIn = {
                    navigation.push(Config.GoogleSignIn)
                },
                onNavigateToGoogleSignOut = {
                    navigation.push(Config.GoogleSignOut)
                },
                onNavigateToConferences = this::showConferences
            )
        )

        is Config.GoogleSignIn -> Child.GoogleSignIn

        is Config.GoogleSignOut -> Child.GoogleSignOut(
            DefaultFirebaseSignOutComponent(
                componentContext,
                onSignedOut = { onUserChanged(null) },
                navigateUp = { navigateUp() }
            )
        )

        is Config.Home -> Child.Home(
            DefaultHomeComponent(
                componentContext,
                config.conference,
                user,
                onSessionSelected = {
                    navigation.push(Config.SessionDetails(user?.uid, config.conference, it))
                },
                onDaySelected = {
                    navigation.push(Config.ConferenceSessions(config.uid, config.conference, it))
                },
                onSettingsSelected = {
                    navigation.push(Config.Settings)
                },
                onBookmarksToggled = {
                    TODO("onBookmarksToggled")
                }
            )
        )

        is Config.Bookmarks -> Child.Bookmarks(
            DefaultBookmarksComponent(
                componentContext,
                config.conference,
                user,
                onSessionSelected = {
                    navigation.push(Config.SessionDetails(user?.uid, config.conference, it))
                }
            )
        )
    }