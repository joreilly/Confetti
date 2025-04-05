package dev.johnoreilly.confetti.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
        if (intent.action == "REMOVE_BOOKMARK") {
            doAsync {
                removeBookmark(intent)
                val notificationId = intent.getIntExtra("notificationId", -1)
                if (notificationId != -1) {
                    notificationManager.cancel(notificationId)
                }
            }
        }
    }

    private suspend fun removeBookmark(intent: Intent?) {
        val conference = intent?.getStringExtra("conference") ?: return
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
}