package dev.johnoreilly.confetti.car.signin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.CarToast.LENGTH_SHORT
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.car.app.model.signin.ProviderSignInMethod
import androidx.car.app.model.signin.SignInTemplate
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.account.SignInProcess
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.car.utils.colorize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SignInScreen(
    carContext: CarContext,
    private val isAuthenticated: Boolean
) : Screen(carContext), KoinComponent {

    private val authentication: Authentication by inject()
    private val signInProcess: SignInProcess by inject()

    override fun onGetTemplate(): Template {
        val providerSignInMethod = if (isAuthenticated) {
            getSignOutAction()
        } else {
            getSignInAction()
        }

        return SignInTemplate.Builder(providerSignInMethod)
            .setTitle(carContext.getString(R.string.auto_sign_in))
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun getSignInAction(): ProviderSignInMethod {
        return ProviderSignInMethod(
            Action.Builder()
                .setTitle(
                    colorize(
                        carContext.getString(R.string.auto_sign_in_google),
                        CarColor.createCustom(Color.BLACK, Color.BLACK),
                        0,
                        carContext.getString(R.string.auto_sign_in_google).length
                    )
                )
                .setBackgroundColor(CarColor.createCustom(Color.WHITE, Color.WHITE))
                .setOnClickListener(ParkedOnlyOnClickListener.create {
                    performSignInWithGoogleFlow(authentication)
                })
                .build()
        )
    }

    private fun getSignOutAction(): ProviderSignInMethod {
        return ProviderSignInMethod(
            Action.Builder()
                .setTitle(
                    colorize(
                        carContext.getString(R.string.auto_sign_out),
                        CarColor.createCustom(Color.BLACK, Color.BLACK),
                        0,
                        carContext.getString(R.string.auto_sign_out).length
                    )
                )
                .setBackgroundColor(CarColor.createCustom(Color.WHITE, Color.WHITE))
                .setOnClickListener(ParkedOnlyOnClickListener.create {
                    // Clears the Credential Manager state as well as the Firebase session
                    CoroutineScope(Dispatchers.Main).launch {
                        signInProcess.signOut()
                        screenManager.popToRoot()
                    }
                })
                .build()
        )
    }

    private fun performSignInWithGoogleFlow(authentication: Authentication) {
        val scope = CoroutineScope(Dispatchers.Main)

        val extras = Bundle(1)
        extras.putBinder(BINDER_KEY, object : SignInWithGoogleActivity.OnSignInComplete() {

            override fun onSignInComplete(signedIn: Boolean) {
                // SignInProcess has already signed in to Firebase; this runs on a binder thread
                scope.launch {
                    if (!signedIn) {
                        CarToast.makeText(carContext, R.string.auto_sign_in_failed, LENGTH_SHORT).show()
                        return@launch
                    }
                    CarToast.makeText(
                        carContext,
                        carContext.getString(
                            R.string.auto_sign_in_success,
                            authentication.currentUser.value?.name.orEmpty()
                        ),
                        LENGTH_SHORT
                    ).show()
                    screenManager.popToRoot()
                }
            }
        })

        runCatching {
            val intent = Intent()
            intent.setClass(carContext, SignInWithGoogleActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtras(extras)
            carContext.startActivity(intent)
        }.getOrElse { error ->
            error.printStackTrace()
        }

        CarToast.makeText(
            carContext,
            carContext.getString(R.string.auto_sign_in_google),
            LENGTH_SHORT
        ).show()
    }
}