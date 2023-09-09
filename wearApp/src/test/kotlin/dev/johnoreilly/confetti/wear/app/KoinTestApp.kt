package dev.johnoreilly.confetti.wear.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import dev.johnoreilly.confetti.wear.ConfettiApplication.Companion.initWearApp
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module

class KoinTestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initWearApp(this, extraModules = listOf(TestAppModule)) {
            // Workmanager uses singleton config
            // So override for tests
            val workConfiguration = Configuration.Builder()
                .setWorkerFactory(KoinWorkerFactory())
                .build()

            WorkManagerTestInitHelper.initializeTestWorkManager(this@KoinTestApp, workConfiguration)
        }
    }

    internal fun loadModules(module: Module, block: () -> Unit) {
        loadKoinModules(module)
        block()
        unloadKoinModules(module)
    }
}