package dev.johnoreilly.confetti.wear.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import dev.johnoreilly.confetti.work.RefreshWorker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkManagerTest : BaseAppTest() {

    @Test
    fun refreshJob() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val workManager = WorkManager.getInstance(context)

        val workRequest = RefreshWorker.oneOff("kotlinconf2023")

        val worker: RefreshWorker = TestListenableWorkerBuilder.from(context, workRequest)
            .setWorkerFactory(workManager.configuration.workerFactory)
            .build() as RefreshWorker

        val result = runBlocking {
            worker.doWork()
        }

        assertEquals(success(), result)
    }
}