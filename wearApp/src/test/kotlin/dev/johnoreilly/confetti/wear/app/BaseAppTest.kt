package dev.johnoreilly.confetti.wear.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.apollographql.apollo3.cache.normalized.sql.ApolloInitializer
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.wear.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode


@RunWith(RobolectricTestRunner::class)
@Config(application = KoinTestApp::class, sdk = [30])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class BaseAppTest : AutoCloseKoinTest() {
    @get:Rule
    val rule = createAndroidComposeRule(MainActivity::class.java)

    val appSettings: AppSettings by inject()

    @Before
    fun setUp() {
        ApolloInitializer().create(get())
    }
}