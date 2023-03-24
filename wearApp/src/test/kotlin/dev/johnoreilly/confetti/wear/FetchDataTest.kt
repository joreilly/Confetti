@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear

import android.util.Log
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.apollographql.apollo3.cache.normalized.sql.ApolloInitializer
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.GetSessionsQuery
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode


@RunWith(RobolectricTestRunner::class)
@Config(application = KoinTestApp::class, sdk = [30])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Ignore("For manual runs")
class FetchDataTest : KoinTest {

    val confettiRepository: ConfettiRepository by inject()
    val apolloClientCache: ApolloClientCache by inject()

    @Before
    fun setUp() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(get(), config)

        ApolloInitializer().create(get())
    }

    @After
    fun after() {
        stopKoin()
    }

    fun GetConferencesQuery.Conference.inspect() {
        "Conference(\"${id}\", listOf(${
            days.joinToString(",") {
                "LocalDate.parse(\"$it\")"
            }
        }), \"${name}\")"
    }

    @Test
    fun fetchConferences() = runTest {
        val conferences =
            apolloClientCache.getClient("all")
                .query(GetConferencesQuery()).execute().dataAssertNoErrors.conferences

        conferences.forEach {
            print(it.inspect())
            println(",")
        }
    }

    fun String?.quoted(): String {
        return if (this != null) {
            "\"$this\""
        } else {
            "null"
        }
    }

    fun SessionDetails.Speaker.inspect(): String {
        return """
            SessionDetails.Speaker(
                __typename = "$__typename",
                speakerDetails = SpeakerDetails(
                    id = ${speakerDetails.id.quoted()},
                    name = ${speakerDetails.name.quoted()},
                    photoUrl = ${speakerDetails.photoUrl.quoted()},
                    company = ${speakerDetails.company.quoted()},
                    companyLogoUrl = ${speakerDetails.companyLogoUrl.quoted()},
                    city = ${speakerDetails.city.quoted()},
                    bio = ${speakerDetails.bio.quoted()},
                    socials = listOf(
                        ${speakerDetails.socials.joinToString(", ") { "SpeakerDetails.Social(${it.name}, ${it.url})" }}
                    )
                )
            )
        """.trimIndent()
    }

    fun SessionDetails.inspect(): String {
        return """
        SessionDetails(
        id = "$id",
        title = ${title.quoted()},
        type = ${type.quoted()},
        startsAt = LocalDateTime.parse(\"$startsAt\"),
        endsAt = LocalDateTime.parse(\"$endsAt\"),
        sessionDescription = ""${""}"$sessionDescription""${""}",
        language = ${language.quoted()},
        speakers = listOf(
            ${speakers.joinToString(",\n") { it.inspect() } }
        ),
        room = SessionDetails.Room(name = "${room?.name}"),
        tags = listOf(${tags.joinToString(", ") { it }}),
        __typename = "$__typename"
    )
        """.trimIndent()
    }

    @Test
    fun fetchSession() = runTest {
        val sessions =
            apolloClientCache.getClient("kotlinconf2023")
                .query(GetSessionsQuery())
                .execute()
                .dataAssertNoErrors.sessions.nodes.map { it.sessionDetails }

        val confetti = sessions.first {
            it.title.contains("Confetti")
        }

        println(confetti.inspect())
    }
}