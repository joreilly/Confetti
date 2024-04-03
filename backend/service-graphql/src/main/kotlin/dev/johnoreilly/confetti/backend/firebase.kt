package dev.johnoreilly.confetti.backend

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import dev.johnoreilly.confetti.backend.datastore.googleCredentials


// Paranoid check
private var initialized = false

@Synchronized
internal fun initializeFirebase() {
    if (!initialized) {
        initialized = true
        val options =
            FirebaseOptions.builder().setCredentials(googleCredentials("firebase_service_account_key.json")).build()
        FirebaseApp.initializeApp(options)
    }
}

fun String.firebaseUid(): String? {
    if (this == "testToken") {
        return "testUser"
    }

    return try {
        FirebaseAuth.getInstance().verifyIdToken(this, true).uid
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
