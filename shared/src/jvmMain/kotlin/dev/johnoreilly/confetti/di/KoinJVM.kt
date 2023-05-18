@file:OptIn(ExperimentalSettingsApi::class, ApolloExperimental::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.DefaultFakeResolver
import com.apollographql.apollo3.api.FakeResolverContext
import com.apollographql.apollo3.network.okHttpClient
import com.apollographql.apollo3.testing.MapTestNetworkTransport
import com.benasher44.uuid.uuid4
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.GetSessionsQuery
import dev.johnoreilly.confetti.schema.__Schema
import dev.johnoreilly.confetti.type.LocalDateTime
import dev.johnoreilly.confetti.type.__CustomScalarAdapters
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.JvmDateService
import kotlinx.datetime.LocalDate
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.prefs.Preferences

actual fun platformModule() = module {
    single<ObservableSettings> { PreferencesSettings(Preferences.userRoot()) }
    single { get<ObservableSettings>().toFlowSettings() }
    singleOf(::JvmDateService) { bind<DateService>() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .build()
    }
    factory {
        ApolloClient.Builder()
            .networkTransport(MapTestNetworkTransport().apply {
                register(GetSessionsQuery(), ApolloResponse.Builder(
                    GetSessionsQuery(),
                    uuid4(),
                    GetSessionsQuery.Data(object: DefaultFakeResolver(__Schema.all) {
                        override fun resolveLeaf(context: FakeResolverContext): Any {
                            return when(context.mergedField.type.rawType().name) {
                                "LocalDateTime" -> return kotlinx.datetime.LocalDateTime(1970, 1, 1, 1, 1, 1)
                                else -> super.resolveLeaf(context)
                            }
                        }
                    }) {}
                ).build())
            })
    }
}

actual fun getDatabaseName(conference: String, uid: String?) = "jdbc:sqlite:$conference$uid.db"
