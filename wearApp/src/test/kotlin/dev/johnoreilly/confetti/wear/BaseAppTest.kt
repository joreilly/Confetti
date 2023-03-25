@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.wear

import android.os.Looper
import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.apollographql.apollo3.cache.normalized.sql.ApolloInitializer
import dev.johnoreilly.confetti.AppSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode


@RunWith(RobolectricTestRunner::class)
@Config(application = KoinTestApp::class, sdk = [30])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class BaseAppTest : KoinTest {
    @get:Rule
    val rule = createAndroidComposeRule(MainActivity::class.java)

    val appSettings: AppSettings by inject()

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
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        stopKoin()
    }
}