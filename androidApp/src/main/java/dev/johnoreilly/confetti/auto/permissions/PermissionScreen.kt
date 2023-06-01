package dev.johnoreilly.confetti.auto.permissions

import android.Manifest.permission
import android.content.pm.PackageManager
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.OnClickListener
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.core.content.ContextCompat
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auto.conferences.ConferencesScreen
import dev.johnoreilly.confetti.auto.navigation.NavigationScreen

class PermissionScreen(
    carContext: CarContext,
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val permissions: MutableList<String> = ArrayList()
        permissions.add(permission.ACCESS_FINE_LOCATION)

        val listener: OnClickListener = ParkedOnlyOnClickListener.create {
            carContext.requestPermissions(
                permissions
            ) { approved: List<String?>, _: List<String?>? ->
                if (approved.isNotEmpty()) {
                    CarToast.makeText(carContext, R.string.auto_permission_granted, CarToast.LENGTH_SHORT).show()
                    invalidate()
                } else {
                    CarToast.makeText(carContext, R.string.auto_permission_not_granted, CarToast.LENGTH_SHORT).show()
                }
            }
        }

        if (ContextCompat.checkSelfPermission(carContext, permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            screenManager.pop()
            screenManager.push(NavigationScreen(carContext))
        }

        return MessageTemplate.Builder(carContext.getString(R.string.auto_permission_message))
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setIcon(CarIcon.ALERT)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(getMoreAction().build())
                    .build()
            )
            .addAction(Action.Builder()
                .setTitle(carContext.getString(R.string.auto_permission_title))
                .setOnClickListener(listener)
                .build()
            )
            .build()
    }

    private fun getMoreAction(): Action.Builder {
        return Action.Builder()
            .setOnClickListener { screenManager.push(ConferencesScreen(carContext)) }
            .setTitle(carContext.getString(R.string.auto_conferences))
    }
}