package dev.johnoreilly.confetti.wear.app

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.firebase.auth.FirebaseUser
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module

val TestAppModule = module {
    single<FirebaseAuthUserRepository> {
        object : FirebaseAuthUserRepository {
            override val firebaseAuthFlow: Flow<FirebaseUser?> = flowOf(null)

            override suspend fun getAuthenticated(): AuthUser? = null

            override suspend fun onSignedIn(account: GoogleSignInAccount) {
            }
        }
    }
}