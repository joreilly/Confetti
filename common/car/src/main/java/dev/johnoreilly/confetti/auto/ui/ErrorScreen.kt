package dev.johnoreilly.confetti.auto.ui

import androidx.annotation.StringRes
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import dev.johnoreilly.confetti.R

class ErrorScreen(
    carContext: CarContext,
    @StringRes val resId: Int,
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder(carContext.getString(resId))
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setIcon(CarIcon.ALERT)
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.auto_retry))
                    .setOnClickListener {
                        screenManager.pop()
                    }
                    .build()
            )
            .build()
    }
}