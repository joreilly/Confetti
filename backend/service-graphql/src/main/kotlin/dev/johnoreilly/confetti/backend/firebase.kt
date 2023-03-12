package dev.johnoreilly.confetti.backend

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import dev.johnoreilly.confetti.backend.datastore.googleCredentials


private val lock = Object()
private var _isInitialized = false

fun String.firebaseUid(): String? {
    if (this == "testToken") {
        return "testUser"
    }

    synchronized(lock) {
        if (!_isInitialized) {
            val options = FirebaseOptions.builder().setCredentials(googleCredentials("firebase_service_account_key.json")).build()
            FirebaseApp.initializeApp(options)
            _isInitialized = true
        }
    }

    return try {
        FirebaseAuth.getInstance().verifyIdToken(this).uid
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
