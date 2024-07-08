package dev.johnoreilly.confetti.wear.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.apollographql.apollo.cache.normalized.sql.ApolloInitializer
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.wear.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
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

    @get:Rule(order = 1)
    val outer: TestRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                ApolloInitializer().create(get())

                base.evaluate()
            }
        }
    }

    @get:Rule(order = 5)
    val configure: TestRule = TestRule { base, description ->
        object : Statement() {
            override fun evaluate() {
                runBlocking {
                    configure(description)
                }
                base.evaluate()
            }
        }
    }

    @get:Rule(order = 10)
    val rule = createAndroidComposeRule(MainActivity::class.java)

    val appSettings: AppSettings by inject()

    open suspend fun configure(description: Description) {
    }
}