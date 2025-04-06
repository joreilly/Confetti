package dev.johnoreilly.confetti.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.concurrent.futures.await
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class NotificationReceiver: BroadcastReceiver(), KoinComponent  {
    private val repository: ConfettiRepository by inject()
    private val appScope: CoroutineScope by inject()
    private val authentication: Authentication by inject()
    private val notificationManager: NotificationManagerCompat by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ActionRemoveBookmark) {
            doAsync {
                removeBookmark(intent)
                val notificationId = intent.getIntExtra("notificationId", -1)
                if (notificationId != -1) {
                    notificationManager.cancel(notificationId)
                }
            }
        } else if (intent.action == ActionOpenOnWear) {
            doAsync {
                openOnWear(intent, context)
            }
        }
    }

    private suspend fun removeBookmark(intent: Intent) {
        val conference = intent.getStringExtra("conferenceId") ?: return
        val sessionId = intent.getStringExtra("sessionId") ?: return
        val user = authentication.currentUser.value ?: return

        repository.removeBookmark(conference, user.uid, user, sessionId)
    }

    private fun BroadcastReceiver.doAsync(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ){
        val pendingResult = goAsync()
        appScope.launch(coroutineContext) { block() }.invokeOnCompletion { pendingResult.finish() }
    }

    private suspend fun openOnWear(intent: Intent, context: Context) {
        val conference = intent.getStringExtra("conferenceId")
        val sessionId = intent.getStringExtra("sessionId")

        val remoteActivityHelper = RemoteActivityHelper(context, Dispatchers.Default.asExecutor())

        val capabilityClient = Wearable.getCapabilityClient(context)
        val installedNodes = capabilityClient.getCapability("confetti_wear_app", CapabilityClient.FILTER_ALL).await().nodes.map { it.id }

        if (installedNodes.isNotEmpty()) {
            remoteActivityHelper.startRemoteActivity(
                Intent(Intent.ACTION_VIEW)
                    .setData("https://confetti-app.dev/conference/$conference/session/$sessionId".toUri())
                    .addCategory(Intent.CATEGORY_BROWSABLE),
                null // all devices
            ).await()
        } else {
            remoteActivityHelper.startRemoteActivity(
                Intent(Intent.ACTION_VIEW)
                    .setData("http://play.google.com/store/apps/details?id=dev.johnoreilly.confetti".toUri())
                    .addCategory(Intent.CATEGORY_BROWSABLE),
                null // all devices
            ).await()
        }
    }

    companion object {
        val ActionRemoveBookmark = "REMOVE_BOOKMARK"
        val ActionOpenOnWear = "OPEN_ON_WEAR"
    }
}