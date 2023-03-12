package dev.johnoreilly.confetti.backend

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import dev.johnoreilly.confetti.backend.datastore.googleCredentials


val localCredentials = googleCredentials("firebase_service_account_key.json")
private var _isInitialized = false
fun String.firebaseUid(): String? {
    synchronized(_isInitialized) {
        if (!_isInitialized) {
            if (localCredentials != null) {
                val options = FirebaseOptions.builder().setCredentials(localCredentials).build()
                FirebaseApp.initializeApp(options)
            } else {
                FirebaseApp.initializeApp()
            }
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
