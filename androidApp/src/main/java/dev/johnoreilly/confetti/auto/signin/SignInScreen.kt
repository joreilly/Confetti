package dev.johnoreilly.confetti.auto.signin

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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auto.utils.colorize
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
                    authentication.signOut()
                    screenManager.popToRoot()
                })
                .build()
        )
    }

    private fun performSignInWithGoogleFlow(authentication: Authentication) {
        val scope = CoroutineScope(Dispatchers.IO)

        val extras = Bundle(1)
        extras.putBinder(BINDER_KEY, object : SignInWithGoogleActivity.OnSignInComplete() {

            @Override
            override fun onSignInComplete(account: GoogleSignInAccount?) {
                if (account == null) {
                    CarToast.makeText(carContext, R.string.auto_sign_in_failed, LENGTH_SHORT).show()
                    return
                }

                scope.launch {
                    authentication.signIn(account.idToken ?: "")
                }

                CarToast.makeText(
                    carContext,
                    carContext.getString(R.string.auto_sign_in_success, account.givenName),
                    LENGTH_SHORT
                ).show()

                screenManager.popToRoot()
            }
        })

        val intent = Intent()
        intent.setClass(carContext, SignInWithGoogleActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtras(extras)
        carContext.startActivity(intent)

        CarToast.makeText(
            carContext,
            carContext.getString(R.string.auto_sign_in_google),
            LENGTH_SHORT
        ).show()
    }
}