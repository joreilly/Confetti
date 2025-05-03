package dev.johnoreilly.confetti.appfunctions;

import android.content.Context
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.AppFunctionManagerCompat
import androidx.appfunctions.AppFunctionSearchSpec
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appsearch.app.GlobalSearchSession
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.platformstorage.PlatformStorage
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeNotNull
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class IntegrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var appFunctionManager: AppFunctionManagerCompat
    private val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation

    @Before
    fun setup(): Unit = runBlocking {
        val appFunctionManagerCompatOrNull = AppFunctionManagerCompat.getInstance(targetContext)
        assumeNotNull(appFunctionManagerCompatOrNull)
        appFunctionManager = checkNotNull(appFunctionManagerCompatOrNull)

        uiAutomation.apply {
            adoptShellPermissionIdentity("android.permission.EXECUTE_APP_FUNCTIONS")
        }
    }

    @Test
    fun listAppSearchDocuments(): Unit = runBlocking {
        val searchSession = createSearchSession(context)

        repeat(10) {
            val searchResults = searchSession.search(
                "",
                SearchSpec.Builder()
                    .addFilterPackageNames("dev.johnoreilly.confetti")
                    .build(),
            )
            var nextPage = searchResults.nextPageAsync.await()
            if (nextPage.isNotEmpty()) {
                while (nextPage.isNotEmpty()) {
                    for (result in nextPage) {
                        println(result.genericDocument)
                    }

                    nextPage = searchResults.nextPageAsync.await()
                }
                return@repeat
            } else {
                delay(1.seconds)
            }
        }

        searchSession.close()
    }

    private suspend fun createSearchSession(context: Context): GlobalSearchSession {
        return PlatformStorage.createGlobalSearchSessionAsync(
            PlatformStorage.GlobalSearchContext.Builder(context).build()
        )
            .await()
    }

    @After
    fun tearDown() {
        uiAutomation.dropShellPermissionIdentity()
    }

    @Test
    fun executeAppFunction_success(): Unit = runBlocking {
        val result = appFunctionManager.observeAppFunctions(AppFunctionSearchSpec()).first()

        result.forEach {
            println(it)
        }

        val response =
            appFunctionManager.executeAppFunction(
                request =
                    ExecuteAppFunctionRequest(
                        targetContext.packageName,
                        "dev.johnoreilly.confetti.appfunctions.ConferenceAppFunctions#conferenceInfo",
                        AppFunctionData.Builder("").build()
                    )
            )

        when (response) {
            is ExecuteAppFunctionResponse.Success -> {
                val returnValue = response.returnValue
                val genericDocument = returnValue.genericDocument.getPropertyDocument("androidAppfunctionsReturnValue")!!
                println(genericDocument.getPropertyStringArray("title")?.toList())
                println(genericDocument.getProperty("dates"))
                println(genericDocument.propertyNames)
            }
            is ExecuteAppFunctionResponse.Error -> throw response.error
        }
    }
}